package team.prediction.learning;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork {

	public double avgError;
	private double errorSmoothingFactor = 100.0;
	public void debug(){
		for(Layer l : layers){
			l.debug();
		}
	}
	private List<Layer> layers;
	private double RateOfTraining;
	private double[] inputScale;
	private double[] outputScale;
	public NeuralNetwork(List<Integer> topology, double rot){
		RateOfTraining = rot;
		layers = new ArrayList<Layer>();
		outputScale = new double[topology.get(topology.size()-1)];
		for(int i = 0 ;i < outputScale.length; ++i){
			outputScale[i] = 1.0;
			System.out.println(outputScale[i]);
		}
		inputScale = new double[topology.get(0)];
		for(int i = 0 ;i < inputScale.length; ++i){
			inputScale[i] = 1.0;
			System.out.println(inputScale[i]);
		}
		for(Integer i : topology){
			int size = i.intValue();
			layers.add(new Layer(size));
		}
		for(int i = 1; i < layers.size(); ++i){
			layers.get(i).AddInputs(layers.get(i-1));
		}
	}
	public NeuralNetwork(String filename){

	}
	public boolean StoreToFile(String filename){
		PrintWriter br = null;
		try{
			br = new PrintWriter(filename, "UTF-8");


			br.close();
			return true;
		}catch(Exception e){
			if(br!=null){
				br.close();
			}
		}
		return false;
	}

	public List<Double> Execute(List<Double> input){

		if(layers.get(0).neurons.size()-1 != input.size()){
			System.err.println("Input size not correct!");
			System.err.println("     Layer: " + (layers.get(0).neurons.size()-1));
			System.err.println("     Input: " + input.size());
			System.out.println(layers.get(-1));
		}
		for(int i = 0 ; i < input.size();++i){
			inputScale[i] = Math.max(Math.abs(input.get(i)), inputScale[i]);
			layers.get(0).neurons.get(i).setOutput(input.get(i)/inputScale[i]);
		}

		//		System.out.println("input: " + input);
		//		System.out.print("normalized input: [");
		//		for(int i = 0 ; i < input.size();++i){
		//			System.out.print(layers.get(0).neurons.get(i).getOutput() + (i+1 == input.size()?" ]\n":", "));
		//		}

		for(int i = 1 ; i < layers.size();++i){
			for(int n = 0; n < layers.get(i).neurons.size()-1; ++n){
				layers.get(i).neurons.get(n).feedForward();
			}
		}

		Layer outputLayer = layers.get(layers.size()-1);
		List<Double> retVal = new ArrayList<Double>();
		for(int i = 0; i < outputLayer.neurons.size()-1; ++i){
			retVal.add(new Double(outputLayer.neurons.get(i).getOutput()*outputScale[i]));
		}
		//		System.out.println("Output: " + retVal);
		return retVal;
	}

	private void averageError(List<Double> targetOutput){
		double m_error = 0.0;
		Layer outputLayer = layers.get(layers.size()-1);

		for(int n = 0 ; n < targetOutput.size() ; ++n)
		{   
			double delta;
			delta = targetOutput.get(n) - outputLayer.neurons.get(n).getOutput();
			m_error += delta * delta;
		}   
		m_error /= outputLayer.neurons.size() - 1;
		m_error = Math.sqrt(m_error);

		// Implement a recent average measurement (optional)
		avgError =
				(avgError * errorSmoothingFactor + m_error)
				/ (errorSmoothingFactor + 1.0);

	}
	public void BackPropagate(List<Double> targetOutput){
		averageError(targetOutput);
		Layer output = layers.get(layers.size()-1);
		assert(targetOutput.size() == output.neurons.size()-1);

		//Calculate gradient for output
		for(int n = 0; n < targetOutput.size(); ++n){
			// fix the outputScale to normalize the internal representation to be between -1 and 1
			if(Math.abs(targetOutput.get(n)) > outputScale[n]){
				outputScale[n] = Math.abs(targetOutput.get(n));
			}
			output.neurons.get(n).calculateGradient(targetOutput.get(n)/outputScale[n]);
		}

		// Calculcate gradient for all hidden layers
		for(int n = layers.size()-2; n > 0; --n){
			Layer curr = layers.get(n);
			Layer front = layers.get(n+1);
			for(int m = 0; m < curr.neurons.size()-1;++m){
				curr.neurons.get(m).calculateGradient(front);
			}
		}

		// Update the input weights of all hidden layers and output layer
		for(int i = layers.size()-1; i > 0; --i){
			Layer curr = layers.get(i);

			for(int j = 0; j < curr.neurons.size()-1; ++j){
				curr.neurons.get(j).updateInputWeights(RateOfTraining);
			}
		}
	}

	public class Layer{
		private List<Neuron> neurons;
		public Layer(int size){
			neurons = new ArrayList<Neuron>();
			for(int i = 0; i < size; ++i){
				neurons.add(new Neuron());
			}
			Neuron bias = new Neuron();
			bias.setOutput(1.0);
			neurons.add(bias);
		}
		public void AddInputs(Layer other){
			for(Neuron o : other.neurons)
			{
				AddInput(o);
			}
		}
		public void AddInput(Neuron other){
			for(Neuron n : neurons){
				n.AddInput(other);
			}
		}
		public List<Neuron> getNeurons(){
			return neurons;
		}
		public void debug(){
			for(Neuron n : neurons){
				n.debug();
			}
			System.out.println("");
		}
	}
}
