package com.company;

import com.company.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import javafx.beans.binding.ObjectExpression;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Defines the interface supported by implementations of the
 * inference algorithms for Bayesian Networks.
 */
public class  MyBNApproxInferencer {

    public Distribution rejection_sampling (BayesianNetwork bn, RandomVariable X, Assignment e,int samplesize){

        List<RandomVariable> list = bn.getVariableListTopologicallySorted();
        List<RandomVariable> looplist = new ArrayList<RandomVariable>();

        double distributionarr[] = new double[X.getDomain().size()];
        for(int i=0;i<distributionarr.length;i++){
            distributionarr[i]=0;
        }
        double validsize=0;

        Assignment assignment = new Assignment();

        //shadow copy
        for (RandomVariable r:list) {
            looplist.add(r);
            assignment.put(r,null);
        }

        for(int i = 0;i<samplesize;i++){
            for(RandomVariable r:looplist){
                double randomnum = Math.random();
                //for each value in domain try to assign and compare probability
                Domain domain=r.getDomain();
                double range= 0;
                for(Object o : domain){
                    assignment.set(r,o);
                    range = range + bn.getProb(r,assignment);
                    if(randomnum<=range) break;
                    
                }
               
            }

            boolean consistent = true;
            for(RandomVariable r:assignment.keySet()){
                if(e.containsKey(r)) {
                    if(assignment.get(r).equals(e.get(r)) == false) {consistent =false;break;}
                }
            }
            if(consistent){
                distributionarr[getbin(X,assignment)]++;
                validsize++;
            }

        }

        Distribution distribution = new Distribution();
        for(int i=0;i<distributionarr.length;i++){
            distributionarr[i]=distributionarr[i]/validsize;
        }
        Domain domain = X.getDomain();
        for(int i=0;i<domain.size();i++){
            distribution.put(domain.get(i),distributionarr[i]);
        }
        distribution.normalize();
        System.out.println(distribution);
        return distribution;
    }

    public int getbin(RandomVariable X,Assignment e){
        Domain domain = X.getDomain();
        Object val = e.get(X);
        for(int i = 0;i< domain.size();i++){
            if(val.equals(domain.get(i))) {return i;}
        }
        return 0;
    }

    public Distribution likelihood_sampling (BayesianNetwork bn, RandomVariable X, Assignment e,int samplesize){

        List<RandomVariable> list = bn.getVariableListTopologicallySorted();
        List<RandomVariable> looplist = new ArrayList<RandomVariable>();

        double distributionarr[] = new double[X.getDomain().size()];
        for(int i=0;i<distributionarr.length;i++){
            distributionarr[i]=0;
        }

        double validsize = 0;

        Assignment assignment = new Assignment();

        //shadow copy
        for (RandomVariable r:list) {
            looplist.add(r);
            assignment.put(r,null);
        }

        for(int i = 0;i<samplesize;i++){
            double weight =1.0;
            for(RandomVariable r:looplist){
                if(isGiven(r,e)) {
                    assignment.set(r,e.get(r));
                    weight=weight*bn.getProb(r,assignment);

                }
                else{
                    double randomnum = Math.random();
                    //for each value in domain try to assign and compare probability
                    Domain domain=r.getDomain();
                    double range= 0;
                    for(Object o : domain){
                        assignment.set(r,o);
                        range = range + bn.getProb(r,assignment);
                        if(randomnum<=range) {break;
                        }
                    }
                }
            }

           
            distributionarr[getbin(X,assignment)]+=weight;
            validsize+=weight;
        }


        Distribution distribution = new Distribution();

        Domain domain = X.getDomain();
        for(int i=0;i<domain.size();i++){
            distribution.put(domain.get(i),distributionarr[i]);
        }
        distribution.normalize();
        System.out.println(distribution);
        return distribution;

    }

    public boolean isGiven(RandomVariable X,Assignment e){

        return e.containsKey(X);
    }





    public static void main(String argv[]) throws IOException, ParserConfigurationException, SAXException{

        XMLBIFParser parser = new XMLBIFParser();

        int samplesize = Integer.parseInt(argv[0]);

        int functioncode = Integer.parseInt((argv[1]));

        BayesianNetwork network = parser.readNetworkFromFile(argv[2]);

        RandomVariable X= network.getVariableByName(argv[3]);

        Assignment e = new Assignment();

        //get the query variable and assign value of it to the assignment map
        //RandomVariable X = network.getVariableByName(argv[3]);

        for(int i = 4; i<argv.length;i+=2){

            //add evidence variable and its assignment to map, also add to given so that we can get correct CPT
            e.put(network.getVariableByName(argv[i]),argv[i+1]);
        }

        MyBNApproxInferencer myBNApproxInferencer = new MyBNApproxInferencer();
        if(functioncode == 1){
            myBNApproxInferencer.rejection_sampling(network,X,e,samplesize);
        }
        if(functioncode == 2){
            myBNApproxInferencer.likelihood_sampling(network,X,e,samplesize);
        }


    }

}
