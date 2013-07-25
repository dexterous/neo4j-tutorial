package org.neo4j.tutorial;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.neo4j.tutorial.matchers.ContainsOnlyHumanCompanions.containsOnlyHumanCompanions;
import static org.neo4j.tutorial.matchers.ContainsOnlySpecificTitles.containsOnlyTitles;

import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

/**
 * In this Koan we start to mix indexing and core API to perform more targeted
 * graph operations. We'll mix indexes and core graph operations to explore the
 * Doctor's universe.
 */
public class Koan05
{

    private static EmbeddedDoctorWhoUniverse universe;

    @BeforeClass
    public static void createDatabase() throws Exception
    {
        universe = new EmbeddedDoctorWhoUniverse( new DoctorWhoUniverseGenerator() );
    }

    @AfterClass
    public static void closeTheDatabase()
    {
        universe.stop();
    }

    @Test
    public void shouldCountTheNumberOfDoctorsRegeneratedForms()
    {

        Index<Node> actorsIndex = universe.getDatabase()
                .index()
                .forNodes( "actors" );
        int numberOfRegenerations = 1;

        // YOUR CODE GOES HERE
        Node doctor = actorsIndex.get("actor", "William Hartnell").getSingle();
        while(doctor.hasRelationship(DoctorWhoRelationships.REGENERATED_TO, Direction.OUTGOING)) {
            numberOfRegenerations++;
            doctor = doctor
                        .getSingleRelationship(DoctorWhoRelationships.REGENERATED_TO, Direction.OUTGOING)
                        .getOtherNode(doctor);
        }

        assertEquals( 11, numberOfRegenerations );
    }

    @Test
    public void shouldFindHumanCompanionsUsingCoreApi()
    {
        HashSet<Node> humanCompanions = new HashSet<Node>();

        // YOUR CODE GOES HERE
        Node theDoctor = universe.getDatabase()
                .index()
                .forNodes( "characters" )
                .get( "character", "Doctor" )
                .getSingle();

        Node humanSpecies = universe.getDatabase()
                .index()
                .forNodes( "species" )
                .get( "species", "Human" )
                .getSingle();

        for(Relationship rel : theDoctor.getRelationships(DoctorWhoRelationships.COMPANION_OF, Direction.INCOMING)) {
            Node companion = rel.getOtherNode(theDoctor);
            for(Relationship speciesRel : companion.getRelationships(DoctorWhoRelationships.IS_A, Direction.OUTGOING)) {
                if(speciesRel.getOtherNode(companion).equals(humanSpecies)) {
                    humanCompanions.add(companion);
                }
            }
        }

        int numberOfKnownHumanCompanions = 40;
        assertEquals( numberOfKnownHumanCompanions, humanCompanions.size() );
        assertThat( humanCompanions, containsOnlyHumanCompanions() );
    }

    @Test
    public void shouldFindAllEpisodesWhereRoseTylerFoughtTheDaleks()
    {
        Index<Node> friendliesIndex = universe.getDatabase()
                .index()
                .forNodes( "characters" );
        Index<Node> speciesIndex = universe.getDatabase()
                .index()
                .forNodes( "species" );
        HashSet<Node> episodesWhereRoseFightsTheDaleks = new HashSet<Node>();

        // YOUR CODE GOES HERE
        Node roseTyler = friendliesIndex.get("character", "Rose Tyler").getSingle();
        Node dalek = speciesIndex.get("species", "Dalek").getSingle();

        for(Relationship rel : roseTyler.getRelationships(DoctorWhoRelationships.APPEARED_IN)) {
            Node episode = rel.getEndNode();
            for(Relationship enemieRel : episode.getRelationships(DoctorWhoRelationships.APPEARED_IN)) {
                if(enemieRel.getStartNode().equals(dalek)) {
                    episodesWhereRoseFightsTheDaleks.add(episode);
                }
            }
        }

        assertThat(
                episodesWhereRoseFightsTheDaleks,
                containsOnlyTitles( "Army of Ghosts", "The Stolen Earth", "Doomsday", "Journey's End", "Bad Wolf",
                        "The Parting of the Ways", "Dalek" ) );
    }
}
