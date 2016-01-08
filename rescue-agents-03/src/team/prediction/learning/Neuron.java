package team.prediction.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import team.prediction.learning.NeuralNetwork.Layer;

public class Neuron{
	
	static Random r = new Random();
	private class Connection{
		public double weight;
//		public double delta;
		public Connection(){
			weight = r.nextDouble(); // range 0-1
//			delta = 0;
			
		}
	}
	
	private double transformFunction(double val){
		return Math.tanh(val);
	}
	private double transformFunctionDerivative(double val){
		double cosh = Math.cosh(val);
		double denominator = Math.cosh(val*2)+1;
		return (4*cosh*cosh)/(denominator*denominator);
	}
	
	
	private double gradient;
	private double output;
	public List<Connection> myInputWeights;
	public List<Neuron> myInputs;
	
	public Neuron(){
		output = 0;
		myInputWeights = new ArrayList<Connection>();
		myInputs = new ArrayList<Neuron>();
	}
	
	public void AddInput(Neuron other){
		myInputs.add(other);
		myInputWeights.add(new Connection());
	}
	

	public void calculateGradient(double target){
		double delta = target-output;
		gradient = delta*transformFunctionDerivative(output);
		
	}

	public void calculateGradient(Layer target){
		
		double dow = sumMyInfluence(target);
		gradient = dow*transformFunctionDerivative(output);
	}
	
	public double sumMyInfluence(Layer fwdLayer){
	
		List<Neuron> neurons = fwdLayer.getNeurons();
		double sum = 0.0;
		for(int i = 0 ; i < neurons.size()-1;++i)
		{
			Neuron curr = neurons.get(i);
			sum += curr.getInfluence(this);
		}
		return sum;
	}
	
	public void updateInputWeights(double RateOfTraining){
		for(int i = 0; i < myInputs.size(); ++i){
			Neuron curr = myInputs.get(i);
			double delta = 
					RateOfTraining *
					curr.output *
					gradient;
			
			myInputWeights.get(i).weight += delta;
		}
	}
	public double getInfluence(Neuron n){
		if(myInputs.contains(n)){
			return myInputWeights.get(myInputs.indexOf(n)).weight*gradient;
		}
		return 0;
	}
	public void feedForward(){
		double sum = 0;
		for(int i = 0 ; i < myInputs.size(); ++i){
			sum += myInputs.get(i).getOutput()*myInputWeights.get(i).weight;
		}
		output = transformFunction(sum);
	}
	public double getOutput(){
		return output;
	}
	public void setOutput(double o){
		output = o;
	}
	
	
	public void debug(){
		for(Connection c : myInputWeights){
			System.out.print(c.weight + ", ");
		}
		System.out.println("");
	}
}
