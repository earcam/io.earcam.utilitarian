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
/**
 * @param ref the index's UUID
 * @param fields a map of maps   fieldName -> fieldProperties, e.g. {title: { boost: 10 }}
 * @returns the builder instance
 */
function createIndexBuilder(ref, fields)
{
	var builder = new lunr.Builder;

	builder.pipeline.add(
		lunr.trimmer,
		lunr.stopWordFilter,
		lunr.stemmer);

	builder.searchPipeline.add(
		lunr.stemmer);

	builder.ref(ref);

	// forEach fields ... grab the key and whack in the map...

	for(var field in fields) {
		builder.field(field, fields[field]);
	}
	// use lunrjs-meddle.html to live/manual "test"
	return builder;
}


/**
 * Adds documents provided by javaDocumentIterator
 * @param builder  the Lunr JS index builder
 * @param javaDocumentIterator an iterator across java.util.Map of field name to field content
 */
function addDocuments(builder, javaDocumentIterator)
{
	while(javaDocumentIterator.hasNext()) {
		builder.add(javaDocumentIterator.next());
	}
}


function buildSerializedIndex(builder)
{
	return serialize(buildIndex(builder));
}


function buildIndex(builder)
{	
	return builder.build();
}


function serialize(object)
{
	return JSON.stringify(object);
}
