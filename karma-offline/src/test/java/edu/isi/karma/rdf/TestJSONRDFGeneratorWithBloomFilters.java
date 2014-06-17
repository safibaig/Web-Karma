/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.BloomFilterKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.KR2RMLBloomFilter;
import edu.isi.karma.kr2rml.KR2RMLBloomFilterManager;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.KarmaException;


/**
 * @author dipsy
 * 
 */
public class TestJSONRDFGeneratorWithBloomFilters extends TestRdfGenerator{

	GenericRDFGenerator rdfGen;
	private static Logger logger = LoggerFactory.getLogger(TestJSONRDFGeneratorWithBloomFilters.class);
	

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		rdfGen = new ContentDetectingRDFGenerator();

		// Add the models in
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
				"people-model", getTestResource(
						 "people-model.ttl"));
		rdfGen.addModel(modelIdentifier);
		
		modelIdentifier = new R2RMLMappingIdentifier("schedule-model",
				 getTestResource("schedule-model.txt")
						);
		rdfGen.addModel(modelIdentifier);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link edu.isi.karma.rdf.JSONRDFGenerator#generateRDF(java.lang.String, java.lang.String, boolean, java.io.PrintWriter)}
	 * .
	 */
	@Test
	public void testGenerateRDF1() {
		try {

			KR2RMLBloomFilterManager peopleBloomFilterManager = getBloomFilterManagerForSource("people.json","people-model");
			KR2RMLBloomFilterManager scheduleBloomFilterManager = getBloomFilterManagerForSource("schedule.csv","schedule-model");
			KR2RMLBloomFilter peoplePersonWithTwitterIdBF = peopleBloomFilterManager.getBloomFilter("http://isi.edu/integration/karma/dev#PredicateObjectMap_1941470a-1dfb-4716-803b-5f07a4af90fd");
			Key k = new Key(("<http://lod.isi.edu/cs548/person/Slepicka>").getBytes());
			assertFalse(peoplePersonWithTwitterIdBF.membershipTest(k));
			k = new Key(("<http://lod.isi.edu/cs548/person/Taheriyan>").getBytes());
			assertTrue(peoplePersonWithTwitterIdBF.membershipTest(k));
			k = new Key(("<http://lod.isi.edu/cs548/person/Kozareva>").getBytes());
			assertFalse(peoplePersonWithTwitterIdBF.membershipTest(k));
			k = new Key(("<http://lod.isi.edu/cs548/person/Ambite>").getBytes());
			assertFalse(peoplePersonWithTwitterIdBF.membershipTest(k));
			k = new Key(("<http://lod.isi.edu/cs548/person/Szekely>").getBytes());
			assertTrue(peoplePersonWithTwitterIdBF.membershipTest(k));
			k = new Key(("<http://lod.isi.edu/cs548/person/Knoblock>").getBytes());
			assertTrue(peoplePersonWithTwitterIdBF.membershipTest(k));
			k = new Key(("<http://lod.isi.edu/cs548/person/Wu>").getBytes());
			assertFalse(peoplePersonWithTwitterIdBF.membershipTest(k));
			assertEquals(3, peoplePersonWithTwitterIdBF.estimateNumberOfHashedValues());
			KR2RMLBloomFilter schedulePersonBF = scheduleBloomFilterManager.getBloomFilter("http://isi.edu/integration/karma/dev#TriplesMap_413a6176-d893-45aa-b1c2-6661b5c491ab");
			k = new Key(("<http://lod.isi.edu/cs548/person/Slepicka>").getBytes());
			assertTrue(schedulePersonBF.membershipTest(k));
			k = new Key(("<http://lod.isi.edu/cs548/person/Taheriyan>").getBytes());
			assertTrue(schedulePersonBF.membershipTest(k));
			k = new Key(("<http://lod.isi.edu/cs548/person/Ambite>").getBytes());
			assertTrue(schedulePersonBF.membershipTest(k));
			k = new Key(("<http://lod.isi.edu/cs548/person/Szekely>").getBytes());
			assertTrue(schedulePersonBF.membershipTest(k));
			k = new Key(("<http://lod.isi.edu/cs548/person/Knoblock>").getBytes());
			assertTrue(schedulePersonBF.membershipTest(k));
			assertEquals(5, schedulePersonBF.estimateNumberOfHashedValues());
			
			KR2RMLBloomFilter intersectionBF = new KR2RMLBloomFilter(1000000,8,Hash.JENKINS_HASH);
			intersectionBF.or(peoplePersonWithTwitterIdBF);
			intersectionBF.and(schedulePersonBF);
			assertEquals(3, intersectionBF.estimateNumberOfHashedValues());
			
			KR2RMLBloomFilter hasInstructorBF = scheduleBloomFilterManager.getBloomFilter("http://isi.edu/integration/karma/dev#RefObjectMap_bb82f923-2953-4bd4-bc7b-d1196e05dbf6");
			
			k = new Key(("<http://lod.isi.edu/cs548/person/Szekely>").getBytes());
			assertTrue(hasInstructorBF.membershipTest(k));
			intersectionBF = new KR2RMLBloomFilter(1000000,8,Hash.JENKINS_HASH);
			intersectionBF.or(hasInstructorBF);
			intersectionBF.and(peoplePersonWithTwitterIdBF);
			assertEquals(3, intersectionBF.estimateNumberOfHashedValues());
			
		} catch (Exception e) {
			logger.error("testGenerateRDF1 failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}

	private KR2RMLBloomFilterManager getBloomFilterManagerForSource(String inputFileName, String modelName)
			throws IOException, URISyntaxException, KarmaException {

		System.out.println("Load file: " + inputFileName);
		String data = EncodingDetector.getString(new File(getTestResource(inputFileName).toURI()),
				"utf-8");

		StringWriter bfsw = new StringWriter();
		PrintWriter bfpw = new PrintWriter(bfsw);

		
		BloomFilterKR2RMLRDFWriter bfWriter = new BloomFilterKR2RMLRDFWriter(bfpw, rdfGen.getModels().get(modelName), false);
		rdfGen.generateRDF(modelName, inputFileName, data, false, bfWriter);
		String base64EncodedBloomFilterManager = bfsw.toString();
		return new KR2RMLBloomFilterManager(new JSONObject(base64EncodedBloomFilterManager));
	}


	private URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
