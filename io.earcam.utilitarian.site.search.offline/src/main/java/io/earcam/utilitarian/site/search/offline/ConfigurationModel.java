/*-
 * #%L
 * io.earcam.utilitarian.site.search.offline
 * %%
 * Copyright (C) 2017 earcam
 * %%
 * SPDX-License-Identifier: (BSD-3-Clause OR EPL-1.0 OR Apache-2.0 OR MIT)
 *
 * You <b>must</b> choose to accept, in full - any individual or combination of
 * the following licenses:
 * <ul>
 * 	<li><a href="https://opensource.org/licenses/BSD-3-Clause">BSD-3-Clause</a></li>
 * 	<li><a href="https://www.eclipse.org/legal/epl-v10.html">EPL-1.0</a></li>
 * 	<li><a href="https://www.apache.org/licenses/LICENSE-2.0">Apache-2.0</a></li>
 * 	<li><a href="https://opensource.org/licenses/MIT">MIT</a></li>
 * </ul>
 * #L%
 */
package io.earcam.utilitarian.site.search.offline;

import static io.earcam.utilitarian.site.search.offline.Crawler.crawler;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

public abstract class ConfigurationModel {

	public static class Step extends ConfigurationModel {}

	public static class Mapping {

		private File dir;
		private URI uri;


		public File getDir()
		{
			return dir;
		}


		static Path dirAsPath(Mapping mapping)
		{
			return mapping.getDir().toPath();
		}


		public void setDir(File dir)
		{
			this.dir = dir;
		}


		public URI getUri()
		{
			return uri;
		}


		public void setUri(URI uri)
		{
			this.uri = uri;
		}
	}

	public static class Indexing extends ConfigurationModel {

		public Indexer build()
		{
			return create(Indexer.class, this);
		}
	}


	static <T extends Component> T create(Class<T> type, ConfigurationModel model)
	{
		T component = serviceById(type, model.getId());
		component.configure(model.getConfiguration());
		return component;
	}


	private static <S extends Component> S serviceById(Class<S> service, String id)
	{
		return serviceStream(service)
				.filter(s -> id.equals(s.id()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Not SPI service with id '" + id
						+ "' found of type '" + service.getCanonicalName() + "'"));
	}


	private static <S extends Component> Stream<S> serviceStream(Class<S> service)
	{
		return stream(ServiceLoader.load(service).spliterator(), false);
	}

	public static class Crawling {

		private List<Mapping> mappings;

		private List<Step> steps;


		public List<Mapping> getMappings()
		{
			return mappings;
		}


		public void setMappings(List<Mapping> mappings)
		{
			this.mappings = mappings;
		}


		public List<Step> getSteps()
		{
			return steps;
		}


		public void setSteps(List<Step> steps)
		{
			this.steps = steps;
		}


		public Crawler build()
		{
			Crawler crawler = crawler(mappings.stream()
					.collect(toMap(Mapping::dirAsPath, Mapping::getUri)));

			Set<String> filters = serviceIds(Filter.class);
			Set<String> processors = serviceIds(Processor.class);

			for(Step step : steps) {
				if(filters.contains(step.getId())) {
					crawler.filter(create(Filter.class, step));

				} else if(processors.contains(step.getId())) {
					crawler.processor(create(Processor.class, step));

				} else {
					throw new IllegalStateException("No filter or processor found via SPI with ID '" + step.getId() + "'");
				}
			}
			return crawler;
		}


		private static <S extends Component> Set<String> serviceIds(Class<S> service)
		{
			return serviceStream(service)
					.map(Component::id)
					.collect(toSet());
		}
	}

	private String id;

	private Map<String, String> configuration;


	public String getId()
	{
		return id;
	}


	public void setId(String id)
	{
		this.id = id;
	}


	public Map<String, String> getConfiguration()
	{
		return configuration;
	}


	public void setConfiguration(Map<String, String> configuration)
	{
		this.configuration = configuration;
	}
}
