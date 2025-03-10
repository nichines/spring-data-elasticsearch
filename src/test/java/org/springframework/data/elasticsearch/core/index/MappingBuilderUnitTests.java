/*
 * Copyright 2013-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.elasticsearch.core.index;

import static org.assertj.core.api.Assertions.*;
import static org.skyscreamer.jsonassert.JSONAssert.*;
import static org.springframework.data.elasticsearch.annotations.FieldType.*;
import static org.springframework.data.elasticsearch.annotations.FieldType.Object;

import java.lang.Boolean;
import java.lang.Double;
import java.lang.Integer;
import java.lang.Object;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.MappingContextBaseTests;
import org.springframework.data.elasticsearch.core.completion.Completion;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mapping.MappingException;
import org.springframework.lang.Nullable;

/**
 * @author Stuart Stevenson
 * @author Jakub Vavrik
 * @author Mohsin Husen
 * @author Keivn Leturc
 * @author Nordine Bittich
 * @author Don Wellington
 * @author Sascha Woo
 * @author Peter-Josef Meisch
 * @author Xiao Yu
 * @author Roman Puchkovskiy
 * @author Brian Kimmig
 * @author Morgan Lutz
 */
public class MappingBuilderUnitTests extends MappingContextBaseTests {

	@Test // DATAES-568
	public void testInfiniteLoopAvoidance() throws JSONException {

		String expected = "{\"properties\":{\"message\":{\"store\":true,\"" + "type\":\"text\",\"index\":false,"
				+ "\"analyzer\":\"standard\"}}}";

		String mapping = getMappingBuilder().buildPropertyMapping(SampleTransientEntity.class);

		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-568
	public void shouldUseValueFromAnnotationType() throws JSONException {

		// Given
		String expected = "{\"properties\":{\"price\":{\"type\":\"double\"}}}";

		// When
		String mapping = getMappingBuilder().buildPropertyMapping(StockPrice.class);

		// Then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-76
	public void shouldBuildMappingWithSuperclass() throws JSONException {

		String expected = "{\"properties\":{\"message\":{\"store\":true,\""
				+ "type\":\"text\",\"index\":false,\"analyzer\":\"standard\"}" + ",\"createdDate\":{"
				+ "\"type\":\"date\",\"index\":false}}}";

		String mapping = getMappingBuilder().buildPropertyMapping(SampleInheritedEntity.class);

		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-285
	public void shouldMapBooks() throws JSONException {

		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"author\": {\n" + //
				"      \"type\": \"object\",\n" + //
				"      \"properties\": {}\n" + //
				"    },\n" + //
				"    \"buckets\": {\n" + //
				"      \"type\": \"nested\"\n" + //
				"    },\n" + //
				"    \"description\": {\n" + //
				"      \"type\": \"text\",\n" + //
				"      \"analyzer\": \"whitespace\",\n" + //
				"      \"fields\": {\n" + //
				"        \"prefix\": {\n" + //
				"          \"type\": \"text\",\n" + //
				"          \"analyzer\": \"stop\",\n" + //
				"          \"search_analyzer\": \"standard\"\n" + //
				"        }\n" + //
				"      }\n" + //
				"    }\n" + //
				"  }\n" + //
				"}"; //

		String mapping = getMappingBuilder().buildPropertyMapping(Book.class);

		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-568, DATAES-929
	@DisplayName("should build mappings for geo types")
	void shouldBuildMappingsForGeoTypes() throws JSONException {

		// given
		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"pointA\": {\n" + //
				"      \"type\": \"geo_point\"\n" + //
				"    },\n" + //
				"    \"pointB\": {\n" + //
				"      \"type\": \"geo_point\"\n" + //
				"    },\n" + //
				"    \"pointC\": {\n" + //
				"      \"type\": \"geo_point\"\n" + //
				"    },\n" + //
				"    \"pointD\": {\n" + //
				"      \"type\": \"geo_point\"\n" + //
				"    },\n" + //
				"    \"shape1\": {\n" + //
				"      \"type\": \"geo_shape\"\n" + //
				"    },\n" + //
				"    \"shape2\": {\n" + //
				"      \"type\": \"geo_shape\",\n" + //
				"      \"orientation\": \"clockwise\",\n" + //
				"      \"ignore_malformed\": true,\n" + //
				"      \"ignore_z_value\": false,\n" + //
				"      \"coerce\": true\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n}"; //

		// when
		String mapping;
		mapping = getMappingBuilder().buildPropertyMapping(GeoEntity.class);

		// then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-568
	public void shouldUseFieldNameOnId() throws JSONException {

		// given
		String expected = "{\"properties\":{" + "\"id-property\":{\"type\":\"keyword\",\"index\":true}" + "}}";

		// when
		String mapping = getMappingBuilder().buildPropertyMapping(FieldNameEntity.IdEntity.class);

		// then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-568
	public void shouldUseFieldNameOnText() throws JSONException {

		// given
		String expected = "{\"properties\":{" + "\"id-property\":{\"type\":\"keyword\",\"index\":true},"
				+ "\"text-property\":{\"type\":\"text\"}" + "}}";

		// when
		String mapping = getMappingBuilder().buildPropertyMapping(FieldNameEntity.TextEntity.class);

		// then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-568
	public void shouldUseFieldNameOnMapping() throws JSONException {

		// given
		String expected = "{\"properties\":{" + "\"id-property\":{\"type\":\"keyword\",\"index\":true},"
				+ "\"mapping-property\":{\"type\":\"string\",\"analyzer\":\"standard_lowercase_asciifolding\"}" + "}}";

		// when
		String mapping = getMappingBuilder().buildPropertyMapping(FieldNameEntity.MappingEntity.class);

		// then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-568
	public void shouldUseFieldNameOnGeoPoint() throws JSONException {

		// given
		String expected = "{\"properties\":{" + "\"id-property\":{\"type\":\"keyword\",\"index\":true},"
				+ "\"geopoint-property\":{\"type\":\"geo_point\"}" + "}}";

		// when
		String mapping = getMappingBuilder().buildPropertyMapping(FieldNameEntity.GeoPointEntity.class);

		// then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-568
	public void shouldUseFieldNameOnCircularEntity() throws JSONException {

		// given
		String expected = "{\"properties\":{" + "\"id-property\":{\"type\":\"keyword\",\"index\":true},"
				+ "\"circular-property\":{\"type\":\"object\",\"properties\":{}}" + "}}";

		// when
		String mapping = getMappingBuilder().buildPropertyMapping(FieldNameEntity.CircularEntity.class);

		// then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-568
	public void shouldUseFieldNameOnCompletion() throws JSONException {

		// given
		String expected = "{\"properties\":{" + "\"id-property\":{\"type\":\"keyword\",\"index\":true}," + //
				"\"completion-property\":{\"type\":\"completion\",\"max_input_length\":100,\"preserve_position_increments\":true,\"preserve_separators\":true,\"search_analyzer\":\"simple\",\"analyzer\":\"simple\"},\"completion-property\":{}"
				+ //
				"}}";

		// when
		String mapping = getMappingBuilder().buildPropertyMapping(FieldNameEntity.CompletionEntity.class);

		// then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-568, DATAES-896
	public void shouldUseFieldNameOnMultiField() throws JSONException {

		// given
		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"id-property\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"index\": true\n" + //
				"    },\n" + //
				"    \"main-field\": {\n" + //
				"      \"type\": \"text\",\n" + //
				"      \"analyzer\": \"whitespace\",\n" + //
				"      \"fields\": {\n" + //
				"        \"suff-ix\": {\n" + //
				"          \"type\": \"text\",\n" + //
				"          \"analyzer\": \"stop\",\n" + //
				"          \"search_analyzer\": \"standard\"\n" + //
				"        }\n" + //
				"      }\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n"; //

		// when
		String mapping = getMappingBuilder().buildPropertyMapping(FieldNameEntity.MultiFieldEntity.class);

		// then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-639
	public void shouldUseIgnoreAbove() throws JSONException {

		// given
		String expected = "{\"properties\":{\"message\":{\"type\":\"keyword\",\"ignore_above\":10}}}";

		// when
		String mapping = getMappingBuilder().buildPropertyMapping(IgnoreAboveEntity.class);

		// then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-621, DATAES-943, DATAES-946
	public void shouldSetFieldMappingProperties() throws JSONException {
		String expected = "{\n" + //
				"        \"properties\": {\n" + //
				"            \"storeTrue\": {\n" + //
				"                \"store\": true\n" + //
				"            },\n" + //
				"            \"indexFalse\": {\n" + //
				"                \"index\": false\n" + //
				"            },\n" + //
				"            \"coerceFalse\": {\n" + //
				"                \"coerce\": false\n" + //
				"            },\n" + //
				"            \"fielddataTrue\": {\n" + //
				"                \"fielddata\": true\n" + //
				"            },\n" + //
				"            \"type\": {\n" + //
				"                \"type\": \"integer\"\n" + //
				"            },\n" + //
				"            \"ignoreAbove\": {\n" + //
				"                \"ignore_above\": 42\n" + //
				"            },\n" + //
				"            \"copyTo\": {\n" + //
				"                \"copy_to\": [\"foo\", \"bar\"]\n" + //
				"            },\n" + //
				"            \"date\": {\n" + //
				"                \"type\": \"date\",\n" + //
				"                \"format\": \"YYYYMMDD\"\n" + //
				"            },\n" + //
				"            \"analyzers\": {\n" + //
				"                \"analyzer\": \"ana\",\n" + //
				"                \"search_analyzer\": \"sana\",\n" + //
				"                \"normalizer\": \"norma\"\n" + //
				"            },\n" + //
				"            \"docValuesTrue\": {\n" + //
				"                \"type\": \"keyword\"\n" + //
				"            },\n" + //
				"            \"docValuesFalse\": {\n" + //
				"                \"type\": \"keyword\",\n" + //
				"                \"doc_values\": false\n" + //
				"            },\n" + //
				"            \"ignoreMalformedTrue\": {\n" + //
				"                \"ignore_malformed\": true\n" + //
				"            },\n" + //
				"            \"indexPhrasesTrue\": {\n" + //
				"                \"index_phrases\": true\n" + //
				"            },\n" + //
				"            \"indexOptionsPositions\": {\n" + //
				"                \"index_options\": \"positions\"\n" + //
				"            },\n" + //
				"            \"defaultIndexPrefixes\": {\n" + //
				"                \"index_prefixes\":{}" + //
				"            },\n" + //
				"            \"customIndexPrefixes\": {\n" + //
				"                \"index_prefixes\":{\"min_chars\":1,\"max_chars\":10}" + //
				"            },\n" + //
				"            \"normsFalse\": {\n" + //
				"                \"norms\": false\n" + //
				"            },\n" + //
				"            \"nullValueString\": {\n" + //
				"                \"null_value\": \"NULLNULL\"\n" + //
				"            },\n" + //
				"            \"nullValueInteger\": {\n" + //
				"                \"null_value\": 42\n" + //
				"            },\n" + //
				"            \"nullValueDouble\": {\n" + //
				"                \"null_value\": 42.0\n" + //
				"            },\n" + //
				"            \"positionIncrementGap\": {\n" + //
				"                \"position_increment_gap\": 42\n" + //
				"            },\n" + //
				"            \"similarityBoolean\": {\n" + //
				"                \"similarity\": \"boolean\"\n" + //
				"            },\n" + //
				"            \"termVectorWithOffsets\": {\n" + //
				"                \"term_vector\": \"with_offsets\"\n" + //
				"            },\n" + //
				"            \"scaledFloat\": {\n" + //
				"                \"type\": \"scaled_float\",\n" + //
				"                \"scaling_factor\": 100.0\n" + //
				"            },\n" + //
				"            \"enabledObject\": {\n" + //
				"                \"type\": \"object\"\n" + //
				"            },\n" + //
				"            \"disabledObject\": {\n" + //
				"                \"type\": \"object\",\n" + //
				"                \"enabled\": false\n" + //
				"            },\n" + //
				"            \"eagerGlobalOrdinalsTrue\": {\n" + //
				"                \"type\": \"text\",\n" + //
				"                \"eager_global_ordinals\": true\n" + //
				"            },\n" + //
				"            \"eagerGlobalOrdinalsFalse\": {\n" + //
				"                \"type\": \"text\"\n" + //
				"            },\n" + //
				"            \"wildcardWithoutParams\": {\n" + //
				"                \"type\": \"wildcard\"\n" + //
				"            },\n" + //
				"            \"wildcardWithParams\": {\n" + //
				"                \"type\": \"wildcard\",\n" + //
				"                \"null_value\": \"WILD\",\n" + //
				"                \"ignore_above\": 42\n" + //
				"            }\n" + //
				"        }\n" + //
				"}\n"; //

		// when
		String mapping = getMappingBuilder().buildPropertyMapping(FieldMappingParameters.class);

		// then
		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-148, #1767
	void shouldWriteDynamicMappingFromAnnotation() throws JSONException {

		String expected = "{\n" + //
				"  \"dynamic\": \"false\",\n" + //
				"  \"properties\": {\n" + //
				"    \"_class\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"index\": false,\n" + //
				"      \"doc_values\": false\n" + //
				"    },\n" + //
				"    \"author\": {\n" + //
				"      \"type\": \"object\",\n" + //
				"      \"dynamic\": \"strict\",\n" + //
				"      \"properties\": {\n" + //
				"        \"_class\": {\n" + //
				"          \"type\": \"keyword\",\n" + //
				"          \"index\": false,\n" + //
				"          \"doc_values\": false\n" + //
				"        }\n" + //
				"      }\n" + //
				"    },\n" + //
				"    \"objectMap\": {\n" + //
				"      \"type\": \"object\",\n" + //
				"      \"dynamic\": \"false\"\n" + //
				"    },\n" + //
				"    \"nestedObjectMap\": {\n" + //
				"      \"type\": \"nested\",\n" + //
				"      \"dynamic\": \"false\"\n" + //
				"    }\n" + //
				"  }\n" + //
				"}"; //

		String mapping = getMappingBuilder().buildPropertyMapping(DynamicMappingAnnotationEntity.class);

		assertEquals(expected, mapping, true);
	}

	@Test // #1871
	void shouldWriteDynamicMapping() throws JSONException {

		String expected = "{\n" //
				+ "  \"dynamic\": \"false\",\n" //
				+ "  \"properties\": {\n" //
				+ "    \"_class\": {\n" //
				+ "      \"type\": \"keyword\",\n" //
				+ "      \"index\": false,\n" //
				+ "      \"doc_values\": false\n" //
				+ "    },\n" //
				+ "    \"objectInherit\": {\n" //
				+ "      \"type\": \"object\"\n" //
				+ "    },\n" //
				+ "    \"objectFalse\": {\n" //
				+ "      \"dynamic\": \"false\",\n" //
				+ "      \"type\": \"object\"\n" //
				+ "    },\n" //
				+ "    \"objectTrue\": {\n" //
				+ "      \"dynamic\": \"true\",\n" //
				+ "      \"type\": \"object\"\n" //
				+ "    },\n" //
				+ "    \"objectStrict\": {\n" //
				+ "      \"dynamic\": \"strict\",\n" //
				+ "      \"type\": \"object\"\n" //
				+ "    },\n" //
				+ "    \"objectRuntime\": {\n" //
				+ "      \"dynamic\": \"runtime\",\n" //
				+ "      \"type\": \"object\"\n" //
				+ "    },\n" //
				+ "    \"nestedObjectInherit\": {\n" //
				+ "      \"type\": \"nested\"\n" //
				+ "    },\n" //
				+ "    \"nestedObjectFalse\": {\n" //
				+ "      \"dynamic\": \"false\",\n" //
				+ "      \"type\": \"nested\"\n" //
				+ "    },\n" //
				+ "    \"nestedObjectTrue\": {\n" //
				+ "      \"dynamic\": \"true\",\n" //
				+ "      \"type\": \"nested\"\n" //
				+ "    },\n" //
				+ "    \"nestedObjectStrict\": {\n" //
				+ "      \"dynamic\": \"strict\",\n" //
				+ "      \"type\": \"nested\"\n" //
				+ "    },\n" //
				+ "    \"nestedObjectRuntime\": {\n" //
				+ "      \"dynamic\": \"runtime\",\n" //
				+ "      \"type\": \"nested\"\n" //
				+ "    }\n" //
				+ "  }\n" //
				+ "}\n" //
				+ "";

		String mapping = getMappingBuilder().buildPropertyMapping(DynamicMappingEntity.class);

		assertEquals(expected, mapping, true);
	}

	@Test // DATAES-784
	void shouldMapPropertyObjectsToFieldDefinition() throws JSONException {
		String expected = "{\n" + //
				"  properties: {\n" + //
				"    valueObject: {\n" + //
				"      type: \"text\"\n" + //
				"    }\n" + //
				"  }\n" + //
				"}";

		String mapping = getMappingBuilder().buildPropertyMapping(ValueDoc.class);

		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-788
	void shouldWriteCompletionContextInfo() throws JSONException {
		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"suggest\": {\n" + //
				"      \"type\": \"completion\",\n" + //
				"      \"contexts\": [\n" + //
				"        {\n" + //
				"          \"name\": \"location\",\n" + //
				"          \"type\": \"geo\",\n" + //
				"          \"path\": \"proppath\"\n" + //
				"        }\n" + //
				"      ]\n" + //
				"    }\n" + //
				"  }\n" + //
				"}";

		String mapping = getMappingBuilder().buildPropertyMapping(CompletionDocument.class);

		assertEquals(expected, mapping, false);
	}

	@Test // DATAES-799
	void shouldNotIncludeSeqNoPrimaryTermPropertyInMappingEvenWhenAnnotatedWithField() {
		String propertyMapping = getMappingBuilder().buildPropertyMapping(EntityWithSeqNoPrimaryTerm.class);

		assertThat(propertyMapping).doesNotContain("seqNoPrimaryTerm");
	}

	@Test // DATAES-854
	@DisplayName("should write rank_feature properties")
	void shouldWriteRankFeatureProperties() throws JSONException {
		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"pageRank\": {\n" + //
				"      \"type\": \"rank_feature\"\n" + //
				"    },\n" + //
				"    \"urlLength\": {\n" + //
				"      \"type\": \"rank_feature\",\n" + //
				"      \"positive_score_impact\": false\n" + //
				"    },\n" + //
				"    \"topics\": {\n" + //
				"      \"type\": \"rank_features\"\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n"; //

		String mapping = getMappingBuilder().buildPropertyMapping(RankFeatureEntity.class);

		assertEquals(expected, mapping, false);
	}

	@Test // #1700
	@DisplayName("should write dense_vector properties")
	void shouldWriteDenseVectorProperties() throws JSONException {
		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"my_vector\": {\n" + //
				"      \"type\": \"dense_vector\",\n" + //
				"      \"dims\": 16\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n"; //

		String mapping = getMappingBuilder().buildPropertyMapping(DenseVectorEntity.class);

		assertEquals(expected, mapping, false);
	}

	@Test // #1370
	@DisplayName("should not write mapping when enabled is false on entity")
	void shouldNotWriteMappingWhenEnabledIsFalseOnEntity() throws JSONException {

		String expected = "{\n" + //
				"  \"enabled\": false" + //
				"}";

		String mapping = getMappingBuilder().buildPropertyMapping(DisabledMappingEntity.class);

		assertEquals(expected, mapping, false);
	}

	@Test // #1370
	@DisplayName("should write disabled property mapping")
	void shouldWriteDisabledPropertyMapping() throws JSONException {

		String expected = "{\n" + //
				"  \"properties\":{\n" + //
				"    \"text\": {\n" + //
				"      \"type\": \"text\"\n" + //
				"    },\n" + //
				"    \"object\": {\n" + //
				"      \"type\": \"object\",\n" + //
				"      \"enabled\": false\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n"; //

		String mapping = getMappingBuilder().buildPropertyMapping(DisabledMappingProperty.class);

		assertEquals(expected, mapping, false);
	}

	@Test // #1370
	@DisplayName("should only allow disabled properties on type object")
	void shouldOnlyAllowDisabledPropertiesOnTypeObject() {

		assertThatThrownBy(() -> getMappingBuilder().buildPropertyMapping(InvalidDisabledMappingProperty.class))
				.isInstanceOf(MappingException.class);
	}

	@Test // #1711
	@DisplayName("should write typeHint entries")
	void shouldWriteTypeHintEntries() throws JSONException {

		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"_class\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"index\": false,\n" + //
				"      \"doc_values\": false\n" + //
				"    },\n" + //
				"    \"id\": {\n" + //
				"      \"type\": \"keyword\"\n" + //
				"    },\n" + //
				"    \"nestedEntity\": {\n" + //
				"      \"type\": \"nested\",\n" + //
				"      \"properties\": {\n" + //
				"        \"_class\": {\n" + //
				"          \"type\": \"keyword\",\n" + //
				"          \"index\": false,\n" + //
				"          \"doc_values\": false\n" + //
				"        },\n" + //
				"        \"nestedField\": {\n" + //
				"          \"type\": \"text\"\n" + //
				"        }\n" + //
				"      }\n" + //
				"    },\n" + //
				"    \"objectEntity\": {\n" + //
				"      \"type\": \"object\",\n" + //
				"      \"properties\": {\n" + //
				"        \"_class\": {\n" + //
				"          \"type\": \"keyword\",\n" + //
				"          \"index\": false,\n" + //
				"          \"doc_values\": false\n" + //
				"        },\n" + //
				"        \"objectField\": {\n" + //
				"          \"type\": \"text\"\n" + //
				"        }\n" + //
				"      }\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n"; //

		String mapping = getMappingBuilder().buildPropertyMapping(TypeHintEntity.class);

		assertEquals(expected, mapping, false);
	}

	@Test // #1727
	@DisplayName("should map according to the annotated properties")
	void shouldMapAccordingToTheAnnotatedProperties() throws JSONException {

		String expected = "{\n" + "    \"properties\": {\n" + //
				"        \"field1\": {\n" + //
				"            \"type\": \"date\",\n" + //
				"            \"format\": \"date_optional_time||epoch_millis\"\n" + //
				"        },\n" + //
				"        \"field2\": {\n" + //
				"            \"type\": \"date\",\n" + //
				"            \"format\": \"basic_date\"\n" + //
				"        },\n" + //
				"        \"field3\": {\n" + //
				"            \"type\": \"date\",\n" + //
				"            \"format\": \"basic_date||basic_time\"\n" + //
				"        },\n" + //
				"        \"field4\": {\n" + //
				"            \"type\": \"date\",\n" + //
				"            \"format\": \"date_optional_time||epoch_millis||dd.MM.uuuu\"\n" + //
				"        },\n" + //
				"        \"field5\": {\n" + //
				"            \"type\": \"date\",\n" + //
				"            \"format\": \"dd.MM.uuuu\"\n" + //
				"        }\n" + //
				"    }\n" + //
				"}"; //

		String mapping = getMappingBuilder().buildPropertyMapping(DateFormatsEntity.class);

		assertEquals(expected, mapping, false);
	}

	@Test // #1454
	@DisplayName("should write type hints when context is configured to do so")
	void shouldWriteTypeHintsWhenContextIsConfiguredToDoSo() throws JSONException {

		((SimpleElasticsearchMappingContext) (elasticsearchConverter.get().getMappingContext())).setWriteTypeHints(true);
		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"_class\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"index\": false,\n" + //
				"      \"doc_values\": false\n" + //
				"    },\n" + //
				"    \"title\": {\n" + //
				"      \"type\": \"text\"\n" + //
				"    },\n" + //
				"    \"authors\": {\n" + //
				"      \"type\": \"nested\",\n" + //
				"      \"properties\": {\n" + //
				"        \"_class\": {\n" + //
				"          \"type\": \"keyword\",\n" + //
				"          \"index\": false,\n" + //
				"          \"doc_values\": false\n" + //
				"        }\n" + //
				"      }\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n"; //

		String mapping = getMappingBuilder().buildPropertyMapping(Magazine.class);

		assertEquals(expected, mapping, true);
	}

	@Test // #1454
	@DisplayName("should not write type hints when context is configured to not do so")
	void shouldNotWriteTypeHintsWhenContextIsConfiguredToNotDoSo() throws JSONException {

		((SimpleElasticsearchMappingContext) (elasticsearchConverter.get().getMappingContext())).setWriteTypeHints(false);
		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"title\": {\n" + //
				"      \"type\": \"text\"\n" + //
				"    },\n" + //
				"    \"authors\": {\n" + //
				"      \"type\": \"nested\",\n" + //
				"      \"properties\": {\n" + //
				"      }\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n"; //

		String mapping = getMappingBuilder().buildPropertyMapping(Magazine.class);

		assertEquals(expected, mapping, true);
	}

	@Test // #1454
	@DisplayName("should write type hints when context is configured to not do so but entity should")
	void shouldWriteTypeHintsWhenContextIsConfiguredToNotDoSoButEntityShould() throws JSONException {

		((SimpleElasticsearchMappingContext) (elasticsearchConverter.get().getMappingContext())).setWriteTypeHints(false);
		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"_class\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"index\": false,\n" + //
				"      \"doc_values\": false\n" + //
				"    },\n" + //
				"    \"title\": {\n" + //
				"      \"type\": \"text\"\n" + //
				"    },\n" + //
				"    \"authors\": {\n" + //
				"      \"type\": \"nested\",\n" + //
				"      \"properties\": {\n" + //
				"        \"_class\": {\n" + //
				"          \"type\": \"keyword\",\n" + //
				"          \"index\": false,\n" + //
				"          \"doc_values\": false\n" + //
				"        }\n" + //
				"      }\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n"; //

		String mapping = getMappingBuilder().buildPropertyMapping(MagazineWithTypeHints.class);

		assertEquals(expected, mapping, true);
	}

	@Test // #1454
	@DisplayName("should not write type hints when context is configured to do so but entity should not")
	void shouldNotWriteTypeHintsWhenContextIsConfiguredToDoSoButEntityShouldNot() throws JSONException {

		((SimpleElasticsearchMappingContext) (elasticsearchConverter.get().getMappingContext())).setWriteTypeHints(true);
		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"title\": {\n" + //
				"      \"type\": \"text\"\n" + //
				"    },\n" + //
				"    \"authors\": {\n" + //
				"      \"type\": \"nested\",\n" + //
				"      \"properties\": {\n" + //
				"      }\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n"; //

		String mapping = getMappingBuilder().buildPropertyMapping(MagazineWithoutTypeHints.class);

		assertEquals(expected, mapping, true);
	}

	@Test // #638
	@DisplayName("should not write dynamic detection mapping entries in default setting")
	void shouldNotWriteDynamicDetectionMappingEntriesInDefaultSetting() throws JSONException {

		String expected = "{\n" + //
				"  \"properties\": {\n" + //
				"    \"_class\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"index\": false,\n" + //
				"      \"doc_values\": false\n" + //
				"    }\n" + //
				"  }\n" + //
				"}"; //

		String mapping = getMappingBuilder().buildPropertyMapping(DynamicDetectionMappingDefault.class);

		assertEquals(expected, mapping, true);
	}

	@Test // #638
	@DisplayName("should write dynamic detection mapping entries when set to false")
	void shouldWriteDynamicDetectionMappingEntriesWhenSetToFalse() throws JSONException {

		String expected = "{\n" + //
				"  \"date_detection\": false," + //
				"  \"numeric_detection\": false," + //
				"  \"properties\": {\n" + //
				"    \"_class\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"index\": false,\n" + //
				"      \"doc_values\": false\n" + //
				"    }\n" + //
				"  }\n" + //
				"}"; //

		String mapping = getMappingBuilder().buildPropertyMapping(DynamicDetectionMappingFalse.class);

		assertEquals(expected, mapping, true);
	}

	@Test // #638
	@DisplayName("should write dynamic detection mapping entries when set to true")
	void shouldWriteDynamicDetectionMappingEntriesWhenSetToTrue() throws JSONException {

		String expected = "{\n" + //
				"  \"date_detection\": true," + //
				"  \"numeric_detection\": true," + //
				"  \"properties\": {\n" + //
				"    \"_class\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"index\": false,\n" + //
				"      \"doc_values\": false\n" + //
				"    }\n" + //
				"  }\n" + //
				"}"; //

		String mapping = getMappingBuilder().buildPropertyMapping(DynamicDetectionMappingTrue.class);

		assertEquals(expected, mapping, true);
	}

	@Test // #638
	@DisplayName("should write dynamic date formats")
	void shouldWriteDynamicDateFormats() throws JSONException {

		String expected = "{\n" + //
				"  \"dynamic_date_formats\": [\"date1\",\"date2\"]," + //
				"  \"properties\": {\n" + //
				"    \"_class\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"index\": false,\n" + //
				"      \"doc_values\": false\n" + //
				"    }\n" + //
				"  }\n" + //
				"}"; //

		String mapping = getMappingBuilder().buildPropertyMapping(DynamicDateFormatsMapping.class);

		assertEquals(expected, mapping, true);
	}

	@Test // #1816
	@DisplayName("should write runtime fields")
	void shouldWriteRuntimeFields() throws JSONException {

		String expected = "{\n" + //
				"  \"runtime\": {\n" + //
				"    \"day_of_week\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"script\": {\n" + //
				"        \"source\": \"emit(doc['@timestamp'].value.dayOfWeekEnum.getDisplayName(TextStyle.FULL, Locale.ROOT))\"\n"
				+ //
				"      }\n" + //
				"    }\n" + //
				"  },\n" + //
				"  \"properties\": {\n" + //
				"    \"_class\": {\n" + //
				"      \"type\": \"keyword\",\n" + //
				"      \"index\": false,\n" + //
				"      \"doc_values\": false\n" + //
				"    },\n" + //
				"    \"@timestamp\": {\n" + //
				"      \"type\": \"date\",\n" + //
				"      \"format\": \"epoch_millis\"\n" + //
				"    }\n" + //
				"  }\n" + //
				"}\n"; //

		String mapping = getMappingBuilder().buildPropertyMapping(RuntimeFieldEntity.class);

		assertEquals(expected, mapping, true);
	}
	// region entities

	@Document(indexName = "ignore-above-index")
	static class IgnoreAboveEntity {
		@Nullable @Id private String id;
		@Nullable @Field(type = FieldType.Keyword, ignoreAbove = 10) private String message;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public String getMessage() {
			return message;
		}

		public void setMessage(@Nullable String message) {
			this.message = message;
		}
	}

	static class FieldNameEntity {

		@Document(indexName = "fieldname-index")
		static class IdEntity {
			@Nullable @Id @Field("id-property") private String id;
		}

		@Document(indexName = "fieldname-index")
		static class TextEntity {

			@Nullable @Id @Field("id-property") private String id;

			@Field(name = "text-property", type = FieldType.Text) //
			@Nullable private String textProperty;
		}

		@Document(indexName = "fieldname-index")
		static class MappingEntity {

			@Nullable @Id @Field("id-property") private String id;

			@Field("mapping-property") @Mapping(mappingPath = "/mappings/test-field-analyzed-mappings.json") //
			@Nullable private byte[] mappingProperty;
		}

		@Document(indexName = "fieldname-index")
		static class GeoPointEntity {

			@Nullable @Id @Field("id-property") private String id;

			@Nullable @Field("geopoint-property") private GeoPoint geoPoint;
		}

		@Document(indexName = "fieldname-index")
		static class CircularEntity {

			@Nullable @Id @Field("id-property") private String id;

			@Nullable @Field(name = "circular-property", type = FieldType.Object, ignoreFields = { "circular-property" }) //
			private CircularEntity circularProperty;
		}

		@Document(indexName = "fieldname-index")
		static class CompletionEntity {

			@Nullable @Id @Field("id-property") private String id;

			@Nullable @Field("completion-property") @CompletionField(maxInputLength = 100) //
			private Completion suggest;
		}

		@Document(indexName = "fieldname-index")
		static class MultiFieldEntity {

			@Nullable @Id @Field("id-property") private String id;

			@Nullable //
			@MultiField(mainField = @Field(name = "main-field", type = FieldType.Text, analyzer = "whitespace"),
					otherFields = {
							@InnerField(suffix = "suff-ix", type = FieldType.Text, analyzer = "stop", searchAnalyzer = "standard") }) //
			private String description;
		}
	}

	@Document(indexName = "test-index-book-mapping-builder")
	static class Book {
		@Nullable @Id private String id;
		@Nullable private String name;
		@Nullable @Field(type = FieldType.Object) private Author author;
		@Nullable @Field(type = FieldType.Nested) private Map<Integer, Collection<String>> buckets = new HashMap<>();
		@Nullable @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "whitespace"),
				otherFields = { @InnerField(suffix = "prefix", type = FieldType.Text, analyzer = "stop",
						searchAnalyzer = "standard") }) private String description;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public String getName() {
			return name;
		}

		public void setName(@Nullable String name) {
			this.name = name;
		}

		@Nullable
		public Author getAuthor() {
			return author;
		}

		public void setAuthor(@Nullable Author author) {
			this.author = author;
		}

		@Nullable
		public Map<java.lang.Integer, Collection<String>> getBuckets() {
			return buckets;
		}

		public void setBuckets(@Nullable Map<java.lang.Integer, Collection<String>> buckets) {
			this.buckets = buckets;
		}

		@Nullable
		public String getDescription() {
			return description;
		}

		public void setDescription(@Nullable String description) {
			this.description = description;
		}
	}

	@Document(indexName = "test-index-simple-recursive-mapping-builder")
	static class SimpleRecursiveEntity {
		@Nullable @Id private String id;
		@Nullable @Field(type = FieldType.Object,
				ignoreFields = { "circularObject" }) private SimpleRecursiveEntity circularObject;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public SimpleRecursiveEntity getCircularObject() {
			return circularObject;
		}

		public void setCircularObject(@Nullable SimpleRecursiveEntity circularObject) {
			this.circularObject = circularObject;
		}
	}

	@Document(indexName = "test-copy-to-mapping-builder")
	static class CopyToEntity {
		@Nullable @Id private String id;
		@Nullable @Field(type = FieldType.Keyword, copyTo = "name") private String firstName;
		@Nullable @Field(type = FieldType.Keyword, copyTo = "name") private String lastName;
		@Nullable @Field(type = FieldType.Keyword) private String name;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(@Nullable String firstName) {
			this.firstName = firstName;
		}

		@Nullable
		public String getLastName() {
			return lastName;
		}

		public void setLastName(@Nullable String lastName) {
			this.lastName = lastName;
		}

		@Nullable
		public String getName() {
			return name;
		}

		public void setName(@Nullable String name) {
			this.name = name;
		}
	}

	@Document(indexName = "test-index-normalizer-mapping-builder")
	@Setting(settingPath = "/settings/test-normalizer.json")
	static class NormalizerEntity {
		@Nullable @Id private String id;
		@Nullable @Field(type = FieldType.Keyword, normalizer = "lower_case_normalizer") private String name;
		@Nullable @MultiField(mainField = @Field(type = FieldType.Text), otherFields = { @InnerField(suffix = "lower_case",
				type = FieldType.Keyword, normalizer = "lower_case_normalizer") }) private String description;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public String getName() {
			return name;
		}

		public void setName(@Nullable String name) {
			this.name = name;
		}

		@Nullable
		public String getDescription() {
			return description;
		}

		public void setDescription(@Nullable String description) {
			this.description = description;
		}
	}

	static class Author {
		@Nullable private String id;
		@Nullable private String name;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@Nullable
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@Document(indexName = "test-index-sample-inherited-mapping-builder")
	static class SampleInheritedEntity extends AbstractInheritedEntity {

		@Nullable @Field(type = Text, index = false, store = true, analyzer = "standard") private String message;

		@Nullable
		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	@Document(indexName = "test-index-stock-mapping-builder")
	static class StockPrice {
		@Nullable @Id private String id;
		@Nullable private String symbol;
		@Nullable @Field(type = FieldType.Double) private BigDecimal price;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(@Nullable String symbol) {
			this.symbol = symbol;
		}

		@Nullable
		public BigDecimal getPrice() {
			return price;
		}

		public void setPrice(@Nullable BigDecimal price) {
			this.price = price;
		}
	}

	static class AbstractInheritedEntity {

		@Nullable @Id private String id;

		@Nullable @Field(type = FieldType.Date, format = DateFormat.date_time, index = false) private Date createdDate;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@Nullable
		public Date getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(Date createdDate) {
			this.createdDate = createdDate;
		}
	}

	@Document(indexName = "test-index-recursive-mapping-mapping-builder")
	static class SampleTransientEntity {

		@Nullable @Id private String id;

		@Nullable @Field(type = Text, index = false, store = true, analyzer = "standard") private String message;

		@Nullable @Transient private NestedEntity nested;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@Nullable
		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		static class NestedEntity {

			@Field private static NestedEntity someField = new NestedEntity();
			@Nullable @Field private Boolean something;

			public NestedEntity getSomeField() {
				return someField;
			}

			public void setSomeField(NestedEntity someField) {
				NestedEntity.someField = someField;
			}

			@Nullable
			public Boolean getSomething() {
				return something;
			}

			public void setSomething(Boolean something) {
				this.something = something;
			}
		}
	}

	@Document(indexName = "test-index-geo-mapping-builder")
	static class GeoEntity {
		@Nullable @Id private String id;
		// geo shape - Spring Data
		@Nullable private Box box;
		@Nullable private Circle circle;
		@Nullable private Polygon polygon;
		// geo point - Custom implementation + Spring Data
		@Nullable @GeoPointField private Point pointA;
		@Nullable private GeoPoint pointB;
		@Nullable @GeoPointField private String pointC;
		@Nullable @GeoPointField private double[] pointD;
		// geo shape, until e have the classes for this, us a strng
		@Nullable @GeoShapeField private String shape1;
		@Nullable @GeoShapeField(coerce = true, ignoreMalformed = true, ignoreZValue = false,
				orientation = GeoShapeField.Orientation.clockwise) private String shape2;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public Box getBox() {
			return box;
		}

		public void setBox(@Nullable Box box) {
			this.box = box;
		}

		@Nullable
		public Circle getCircle() {
			return circle;
		}

		public void setCircle(@Nullable Circle circle) {
			this.circle = circle;
		}

		@Nullable
		public Polygon getPolygon() {
			return polygon;
		}

		public void setPolygon(@Nullable Polygon polygon) {
			this.polygon = polygon;
		}

		@Nullable
		public Point getPointA() {
			return pointA;
		}

		public void setPointA(@Nullable Point pointA) {
			this.pointA = pointA;
		}

		@Nullable
		public GeoPoint getPointB() {
			return pointB;
		}

		public void setPointB(@Nullable GeoPoint pointB) {
			this.pointB = pointB;
		}

		@Nullable
		public String getPointC() {
			return pointC;
		}

		public void setPointC(@Nullable String pointC) {
			this.pointC = pointC;
		}

		@Nullable
		public double[] getPointD() {
			return pointD;
		}

		public void setPointD(@Nullable double[] pointD) {
			this.pointD = pointD;
		}

		@Nullable
		public String getShape1() {
			return shape1;
		}

		public void setShape1(@Nullable String shape1) {
			this.shape1 = shape1;
		}

		@Nullable
		public String getShape2() {
			return shape2;
		}

		public void setShape2(@Nullable String shape2) {
			this.shape2 = shape2;
		}
	}

	@Document(indexName = "test-index-field-mapping-parameters")
	static class FieldMappingParameters {
		@Nullable @Field private String indexTrue;
		@Nullable @Field(index = false) private String indexFalse;
		@Nullable @Field(store = true) private String storeTrue;
		@Nullable @Field private String storeFalse;
		@Nullable @Field private String coerceTrue;
		@Nullable @Field(coerce = false) private String coerceFalse;
		@Nullable @Field(fielddata = true) private String fielddataTrue;
		@Nullable @Field private String fielddataFalse;
		@Nullable @Field(copyTo = { "foo", "bar" }) private String copyTo;
		@Nullable @Field(ignoreAbove = 42) private String ignoreAbove;
		@Nullable @Field(type = FieldType.Integer) private String type;
		@Nullable @Field(type = FieldType.Date, format = {}, pattern = "YYYYMMDD") private LocalDate date;
		@Nullable @Field(analyzer = "ana", searchAnalyzer = "sana", normalizer = "norma") private String analyzers;
		@Nullable @Field(type = Keyword) private String docValuesTrue;
		@Nullable @Field(type = Keyword, docValues = false) private String docValuesFalse;
		@Nullable @Field(ignoreMalformed = true) private String ignoreMalformedTrue;
		@Nullable @Field() private String ignoreMalformedFalse;
		@Nullable @Field(indexOptions = IndexOptions.none) private String indexOptionsNone;
		@Nullable @Field(indexOptions = IndexOptions.positions) private String indexOptionsPositions;
		@Nullable @Field(indexPhrases = true) private String indexPhrasesTrue;
		@Nullable @Field() private String indexPhrasesFalse;
		@Nullable @Field(indexPrefixes = @IndexPrefixes) private String defaultIndexPrefixes;
		@Nullable @Field(indexPrefixes = @IndexPrefixes(minChars = 1, maxChars = 10)) private String customIndexPrefixes;
		@Nullable @Field private String normsTrue;
		@Nullable @Field(norms = false) private String normsFalse;
		@Nullable @Field private String nullValueNotSet;
		@Nullable @Field(nullValue = "NULLNULL") private String nullValueString;
		@Nullable @Field(nullValue = "42", nullValueType = NullValueType.Integer) private String nullValueInteger;
		@Nullable @Field(nullValue = "42.0", nullValueType = NullValueType.Double) private String nullValueDouble;
		@Nullable @Field(positionIncrementGap = 42) private String positionIncrementGap;
		@Nullable @Field private String similarityDefault;
		@Nullable @Field(similarity = Similarity.Boolean) private String similarityBoolean;
		@Nullable @Field private String termVectorDefault;
		@Nullable @Field(termVector = TermVector.with_offsets) private String termVectorWithOffsets;
		@Nullable @Field(type = FieldType.Scaled_Float, scalingFactor = 100.0) Double scaledFloat;
		@Nullable @Field(type = Auto) String autoField;
		@Nullable @Field(type = Object, enabled = true) private String enabledObject;
		@Nullable @Field(type = Object, enabled = false) private String disabledObject;
		@Nullable @Field(type = Text, eagerGlobalOrdinals = true) private String eagerGlobalOrdinalsTrue;
		@Nullable @Field(type = Text, eagerGlobalOrdinals = false) private String eagerGlobalOrdinalsFalse;
		@Nullable @Field(type = Wildcard) private String wildcardWithoutParams;
		@Nullable @Field(type = Wildcard, nullValue = "WILD", ignoreAbove = 42) private String wildcardWithParams;
	}

	@Document(indexName = "test-index-configure-dynamic-mapping")
	@DynamicMapping(DynamicMappingValue.False)
	static class DynamicMappingAnnotationEntity {

		@Nullable @DynamicMapping(DynamicMappingValue.Strict) @Field(type = FieldType.Object) private Author author;
		@Nullable @DynamicMapping(DynamicMappingValue.False) @Field(
				type = FieldType.Object) private Map<String, Object> objectMap;
		@Nullable @DynamicMapping(DynamicMappingValue.False) @Field(
				type = FieldType.Nested) private List<Map<String, Object>> nestedObjectMap;

		@Nullable
		public Author getAuthor() {
			return author;
		}

		public void setAuthor(Author author) {
			this.author = author;
		}
	}

	@Document(indexName = "test-index-configure-dynamic-mapping", dynamic = Dynamic.FALSE)
	static class DynamicMappingEntity {

		@Nullable @Field(type = FieldType.Object) //
		private Map<String, Object> objectInherit;
		@Nullable @Field(type = FieldType.Object, dynamic = Dynamic.FALSE) //
		private Map<String, Object> objectFalse;
		@Nullable @Field(type = FieldType.Object, dynamic = Dynamic.TRUE) //
		private Map<String, Object> objectTrue;
		@Nullable @Field(type = FieldType.Object, dynamic = Dynamic.STRICT) //
		private Map<String, Object> objectStrict;
		@Nullable @Field(type = FieldType.Object, dynamic = Dynamic.RUNTIME) //
		private Map<String, Object> objectRuntime;
		@Nullable @Field(type = FieldType.Nested) //
		private List<Map<String, Object>> nestedObjectInherit;
		@Nullable @Field(type = FieldType.Nested, dynamic = Dynamic.FALSE) //
		private List<Map<String, Object>> nestedObjectFalse;
		@Nullable @Field(type = FieldType.Nested, dynamic = Dynamic.TRUE) //
		private List<Map<String, Object>> nestedObjectTrue;
		@Nullable @Field(type = FieldType.Nested, dynamic = Dynamic.STRICT) //
		private List<Map<String, Object>> nestedObjectStrict;
		@Nullable @Field(type = FieldType.Nested, dynamic = Dynamic.RUNTIME) //
		private List<Map<String, Object>> nestedObjectRuntime;

	}

	static class ValueObject {
		private final String value;

		public ValueObject(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	@Document(indexName = "valueDoc")
	static class ValueDoc {
		@Nullable @Field(type = Text) private ValueObject valueObject;
	}

	@Document(indexName = "completion")
	static class CompletionDocument {
		@Nullable @Id private String id;
		@Nullable @CompletionField(contexts = { @CompletionContext(name = "location",
				type = CompletionContext.ContextMappingType.GEO, path = "proppath") }) private Completion suggest;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public Completion getSuggest() {
			return suggest;
		}

		public void setSuggest(@Nullable Completion suggest) {
			this.suggest = suggest;
		}
	}

	@Document(indexName = "test-index-entity-with-seq-no-primary-term-mapping-builder")
	static class EntityWithSeqNoPrimaryTerm {
		@Field(type = Object) private SeqNoPrimaryTerm seqNoPrimaryTerm;

		public SeqNoPrimaryTerm getSeqNoPrimaryTerm() {
			return seqNoPrimaryTerm;
		}

		public void setSeqNoPrimaryTerm(SeqNoPrimaryTerm seqNoPrimaryTerm) {
			this.seqNoPrimaryTerm = seqNoPrimaryTerm;
		}
	}

	static class RankFeatureEntity {
		@Nullable @Id private String id;
		@Nullable @Field(type = FieldType.Rank_Feature) private Integer pageRank;
		@Nullable @Field(type = FieldType.Rank_Feature, positiveScoreImpact = false) private Integer urlLength;
		@Nullable @Field(type = FieldType.Rank_Features) private Map<String, Integer> topics;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public java.lang.Integer getPageRank() {
			return pageRank;
		}

		public void setPageRank(@Nullable java.lang.Integer pageRank) {
			this.pageRank = pageRank;
		}

		@Nullable
		public java.lang.Integer getUrlLength() {
			return urlLength;
		}

		public void setUrlLength(@Nullable java.lang.Integer urlLength) {
			this.urlLength = urlLength;
		}

		@Nullable
		public Map<String, java.lang.Integer> getTopics() {
			return topics;
		}

		public void setTopics(@Nullable Map<String, java.lang.Integer> topics) {
			this.topics = topics;
		}
	}

	static class DenseVectorEntity {
		@Nullable @Id private String id;
		@Nullable @Field(type = FieldType.Dense_Vector, dims = 16) private float[] my_vector;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public float[] getMy_vector() {
			return my_vector;
		}

		public void setMy_vector(@Nullable float[] my_vector) {
			this.my_vector = my_vector;
		}
	}

	@Mapping(enabled = false)
	static class DisabledMappingEntity {
		@Nullable @Id private String id;
		@Nullable @Field(type = Text) private String text;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public String getText() {
			return text;
		}

		public void setText(@Nullable String text) {
			this.text = text;
		}
	}

	static class InvalidDisabledMappingProperty {
		@Nullable @Id private String id;
		@Nullable @Mapping(enabled = false) @Field(type = Text) private String text;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public String getText() {
			return text;
		}

		public void setText(@Nullable String text) {
			this.text = text;
		}
	}

	static class DisabledMappingProperty {
		@Nullable @Id private String id;
		@Nullable @Field(type = Text) private String text;
		@Nullable @Mapping(enabled = false) @Field(type = Object) private Object object;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public String getText() {
			return text;
		}

		public void setText(@Nullable String text) {
			this.text = text;
		}

		@Nullable
		public java.lang.Object getObject() {
			return object;
		}

		public void setObject(@Nullable java.lang.Object object) {
			this.object = object;
		}
	}

	static class TypeHintEntity {
		@Nullable @Id @Field(type = Keyword) private String id;
		@Nullable @Field(type = Nested) private NestedEntity nestedEntity;
		@Nullable @Field(type = Object) private ObjectEntity objectEntity;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public NestedEntity getNestedEntity() {
			return nestedEntity;
		}

		public void setNestedEntity(@Nullable NestedEntity nestedEntity) {
			this.nestedEntity = nestedEntity;
		}

		@Nullable
		public ObjectEntity getObjectEntity() {
			return objectEntity;
		}

		public void setObjectEntity(@Nullable ObjectEntity objectEntity) {
			this.objectEntity = objectEntity;
		}

		static class NestedEntity {
			@Nullable @Field(type = Text) private String nestedField;

			@Nullable
			public String getNestedField() {
				return nestedField;
			}

			public void setNestedField(@Nullable String nestedField) {
				this.nestedField = nestedField;
			}
		}

		static class ObjectEntity {
			@Nullable @Field(type = Text) private String objectField;

			@Nullable
			public String getObjectField() {
				return objectField;
			}

			public void setObjectField(@Nullable String objectField) {
				this.objectField = objectField;
			}
		}
	}

	static class DateFormatsEntity {
		@Nullable @Id private String id;
		@Nullable @Field(type = FieldType.Date) private LocalDateTime field1;
		@Nullable @Field(type = FieldType.Date, format = DateFormat.basic_date) private LocalDateTime field2;
		@Nullable @Field(type = FieldType.Date,
				format = { DateFormat.basic_date, DateFormat.basic_time }) private LocalDateTime field3;
		@Nullable @Field(type = FieldType.Date, pattern = "dd.MM.uuuu") private LocalDateTime field4;
		@Nullable @Field(type = FieldType.Date, format = {}, pattern = "dd.MM.uuuu") private LocalDateTime field5;

		@Nullable
		public String getId() {
			return id;
		}

		public void setId(@Nullable String id) {
			this.id = id;
		}

		@Nullable
		public LocalDateTime getField1() {
			return field1;
		}

		public void setField1(@Nullable LocalDateTime field1) {
			this.field1 = field1;
		}

		@Nullable
		public LocalDateTime getField2() {
			return field2;
		}

		public void setField2(@Nullable LocalDateTime field2) {
			this.field2 = field2;
		}

		@Nullable
		public LocalDateTime getField3() {
			return field3;
		}

		public void setField3(@Nullable LocalDateTime field3) {
			this.field3 = field3;
		}

		@Nullable
		public LocalDateTime getField4() {
			return field4;
		}

		public void setField4(@Nullable LocalDateTime field4) {
			this.field4 = field4;
		}

		@Nullable
		public LocalDateTime getField5() {
			return field5;
		}

		public void setField5(@Nullable LocalDateTime field5) {
			this.field5 = field5;
		}
	}

	@Document(indexName = "magazine")
	private static class Magazine {
		@Id @Nullable private String id;
		@Field(type = Text) @Nullable private String title;
		@Field(type = Nested) @Nullable private List<Author> authors;
	}

	@Document(indexName = "magazine-without-type-hints", writeTypeHint = WriteTypeHint.FALSE)
	private static class MagazineWithoutTypeHints {
		@Id @Nullable private String id;
		@Field(type = Text) @Nullable private String title;
		@Field(type = Nested) @Nullable private List<Author> authors;
	}

	@Document(indexName = "magazine-with-type-hints", writeTypeHint = WriteTypeHint.TRUE)
	private static class MagazineWithTypeHints {
		@Id @Nullable private String id;
		@Field(type = Text) @Nullable private String title;
		@Field(type = Nested) @Nullable private List<Author> authors;
	}

	@Document(indexName = "dynamic-field-mapping-default")
	private static class DynamicDetectionMappingDefault {
		@Id @Nullable private String id;
	}

	@Document(indexName = "dynamic-dateformats-mapping")
	@Mapping(dynamicDateFormats = { "date1", "date2" })
	private static class DynamicDateFormatsMapping {
		@Id @Nullable private String id;
	}

	@Document(indexName = "dynamic-detection-mapping-true")
	@Mapping(dateDetection = Mapping.Detection.TRUE, numericDetection = Mapping.Detection.TRUE)
	private static class DynamicDetectionMappingTrue {
		@Id @Nullable private String id;
	}

	@Document(indexName = "dynamic-detection-mapping-false")
	@Mapping(dateDetection = Mapping.Detection.FALSE, numericDetection = Mapping.Detection.FALSE)
	private static class DynamicDetectionMappingFalse {
		@Id @Nullable private String id;
	}

	@Document(indexName = "runtime-fields")
	@Mapping(runtimeFieldsPath = "/mappings/runtime-fields.json")
	private static class RuntimeFieldEntity {
		@Id @Nullable private String id;
		@Field(type = Date, format = DateFormat.epoch_millis, name = "@timestamp") @Nullable private Instant timestamp;
	}
	// endregion
}
