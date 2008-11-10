package opennlp.perceptron;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import opennlp.model.AbstractModel;
import opennlp.model.Context;
import opennlp.model.EvalParameters;

public class PerceptronModel extends AbstractModel {

  
  public PerceptronModel(Context[] params, String[] predLabels, String[] outcomeNames) {
    super(params,predLabels,outcomeNames);
    modelType = ModelType.Perceptron;
  }
  
  public double[] eval(String[] context) {
    return eval(context,new double[evalParams.getNumOutcomes()]);
  }
  
  public double[] eval(String[] context, float[] values) {
    return eval(context,values,new double[evalParams.getNumOutcomes()]);
  }

  public double[] eval(String[] context, double[] probs) {
    return eval(context,null,probs);
  }

  public double[] eval(String[] context, float[] values,double[] outsums) {
    int[] scontexts = new int[context.length];
    java.util.Arrays.fill(outsums, 0);
    for (int i=0; i<context.length; i++) {
      Integer ci = pmap.get(context[i]);
      scontexts[i] = ci == null ? -1 : ci;
    }
    return eval(scontexts,values,outsums,evalParams,true);
  }
  
  public static double[] eval(int[] context, double[] prior, EvalParameters model) {
    return eval(context,null,prior,model,true);
  }
  
  public static double[] eval(int[] context, float[] values, double[] prior, EvalParameters model, boolean normalize) {
    Context[] params = model.getParams();
    double[] activeParameters;
    int[] activeOutcomes;
    double value = 1;
    for (int ci = 0; ci < context.length; ci++) {
      if (context[ci] >= 0) {
        Context predParams = params[context[ci]];
        activeOutcomes = predParams.getOutcomes();
        activeParameters = predParams.getParameters();
        if (values != null) {
          value = values[ci];
        }
        for (int ai = 0; ai < activeOutcomes.length; ai++) {
          int oid = activeOutcomes[ai];
          prior[oid] += activeParameters[ai] * value;
        }
      }
    }    
    if (normalize) {
      double normal = 0.0;
      double min = prior[0];
      for (int oid = 0; oid < model.getNumOutcomes(); oid++) {
        if (prior[oid] < min) {
          min = prior[oid];
        }
      }
      for (int oid = 0; oid < model.getNumOutcomes(); oid++) {
        if (min < 0) {
          prior[oid]+=(-1*min);
        }
        normal += prior[oid];
      }
      if (normal == 0.0) {
        for (int oid = 0; oid < model.getNumOutcomes(); oid++) {
          prior[oid] = (double) 1/model.getNumOutcomes();
        }
      }
      else {
        for (int oid = 0; oid < model.getNumOutcomes(); oid++) {
          prior[oid] /= normal;
        }
      }
    }
    //System.err.println("Perceptron Model: "+java.util.Arrays.asList(prior));
    return prior;
  }
  
  public static void main(String[] args) throws java.io.IOException {
    if (args.length == 0) {
      System.err.println("Usage: PerceptronModel modelname < contexts");
      System.exit(1);
    }
    AbstractModel m = new PerceptronModelReader(new File(args[0])).getModel();
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    DecimalFormat df = new java.text.DecimalFormat(".###");
    for (String line = in.readLine(); line != null; line = in.readLine()) {
      String[] context = line.split(" ");
      double[] dist = m.eval(context);
      for (int oi=0;oi<dist.length;oi++) {
        System.out.print("["+m.getOutcome(oi)+" "+df.format(dist[oi])+"] ");
      }
      System.out.println();
    }
  }
}
