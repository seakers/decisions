package graph.decision;

import graph.Decision;
import org.neo4j.driver.Record;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class Connecting extends Decision {

    public static class Builder extends Decision.Builder<Connecting.Builder>{

        public Builder(Record node){
            super(node);
        }

        public Connecting build() { return new Connecting(this); }
    }

    protected Connecting(Connecting.Builder builder){
        super(builder);



    }








}
