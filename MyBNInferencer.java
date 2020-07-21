package com.company;

import com.company.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Defines the interface supported by implementations of the
 * inference algorithms for Bayesian Networks.
 */
public class MyBNInferencer {

    /*
     * Returns the Distribution of the query RandomVariable X
     * given evidence Assignment e, using the distribution encoded
     * by the BayesianNetwork bn.
     * Note that some algorithms may require additional parameters, for example
     * the number of samples for approximate inferencers. You can have methods
     * that accept those parameters and use them in your testing. Just implement
     * this method using some reasonable default value to satisfy the interface.
     * Or don't implement this interface at all. It's really here to guide you
     * as to what an inferencer should do (namely, compute a posterior distriubution.
     */

    public Distribution ask(BayesianNetwork bn, RandomVariable X, Assignment e){

        List<RandomVariable> list = bn.getVariableList();
        List<RandomVariable> looplist = new ArrayList<RandomVariable>();

        //shadow copy variable in the network to a new list
        for(RandomVariable R : list){
            looplist.add(R);
        }

        //filter out query variable and the rest evidence variables since they are not looping
        Iterator<RandomVariable> iterator = e.variableSet().iterator();
        while(iterator.hasNext()){
            looplist.remove(iterator.next());
        };
        looplist.remove(X);

        //make sure query variable is the first one


        //initialize slot for rest of the variables
        e.put(X,null);
        for(RandomVariable R : looplist){
            e.put(R,null);
        }

        Distribution distribution = new Distribution(X);


        for(Object o: X.getDomain()){
            e.set(X,o);
            distribution.put(o,loop(bn,looplist,e,0,0));
        }

        distribution.normalize();
        System.out.println(distribution);
        return distribution;

    };

    public double loop(BayesianNetwork network,List<RandomVariable> list,Assignment e,int index,double sum){
        if(index >= list.size()){
            return 0;
        }

        RandomVariable currentVariable = list.get(index);


        double sum2 =0;
        //for each value in the domain of this variable
        for(Object o : currentVariable.getDomain()){
            e.set(currentVariable,o);

            sum =loop(network,list,e,index+1,sum)+sum;
            //System.out.println("Total Sum is "+sum);

            //if this is the last looping vatiable and value assigned then calculate conditional probability
            if(index == list.size()-1){
                double prob=1;
                for(RandomVariable R :network.getVariableList()){
                    prob = prob * network.getProb(R,e);
                }
                System.out.print(e+" Probability is: "+ prob);
                System.out.println();
                sum2 = sum2+ prob;
            }

        }

        if(index != list.size()-1){return sum;};
        if(index == list.size()-1) return  sum2;
        return sum2;

    };



    public static void main(String argv[]) throws IOException, ParserConfigurationException, SAXException{
        XMLBIFParser parser = new XMLBIFParser();

        BayesianNetwork network = parser.readNetworkFromFile(argv[0]);

        List<RandomVariable> givens = new ArrayList<RandomVariable>(argv.length-3);
        Assignment e = new Assignment();

        //get the query variable and assign value of it to the assignment map
        RandomVariable X = network.getVariableByName(argv[1]);

        for(int i = 2; i<argv.length;i+=2){

            //add evidence variable and its assignment to map, also add to given so that we can get correct CPT
            e.put(network.getVariableByName(argv[i]),argv[i+1]);
            givens.add((network.getVariableByName(argv[i])));
        }


        MyBNInferencer bninferencer =new MyBNInferencer();
        bninferencer.ask(network,X,e);

    }

}
