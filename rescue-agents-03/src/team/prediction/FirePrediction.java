package team.prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityConstants;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import team.AbstractCentre;
//import team.prediction.learning.BuildingData;
import team.prediction.learning.NeuralNetwork;

public class FirePrediction {

	static int MAX_DISTANCE = 25000;
	/**
	 * Internal representation of the timeSteps
	 */
	private int timeStep;
	/**
	 * The world model
	 */
	private StandardWorldModel model;
	/**
	 * Determine if it's practicing or running
	 */
	public boolean PRACTICE = true;
	/**
	 * Data of each tick while practicing
	 */
	public HashMap<Integer, List<FireBuilding>> practiceData;
	/**
	 * All fires detected by agents
	 */
	private Set<EntityID> discoveredFires;
	/**
	 * Internal representation of all discovered fires
	 */
	private List<FireBuilding> discoveredFireBuildings;
	/**
	 * Internal storage for all existing predicted fires
	 */
	private List<FireBuilding> predictedFireBuildings;
	/**
	 * Internal storage for all buildings that are starting to heat up
	 */
	private Map<EntityID, FireBuilding> possibleFireBuildings;
	/**
	 * Internal storage for all buildings that have burned up
	 */
	private Set<EntityID> burntoutFireBuildings;
	/**
	 * Internal storage for all buildings that have been marked as protected.
	 * Being marked as protected means that the fire cannot spread to this building
	 */
	private Set<EntityID> protectedFires;
	/**
	 * A new representation of the distance between building,
	 * This representation is for closest touching distance between all buildings
	 */
	private TDistance touchingDistance;

	/**
	 * A neural network for determining how the fire is expanding
	 */
	private NeuralNetwork nnFireExpansion;
	/**
	 * A neural network that determines if a building will catch on fire or not
	 */
	private NeuralNetwork nnNewFireOccurence;

	public FirePrediction(AbstractCentre centre){
		this.model = centre.GetModel();
		init();

		if(PRACTICE){
			fixAllExistingFires();
			practiceData = new HashMap<Integer, List<FireBuilding>>();
		}
		setup();
	}

	public FirePrediction(StandardWorldModel model){
		this.model = model;
		init();

		if(PRACTICE){
			fixAllExistingFires();
			practiceData = new HashMap<Integer, List<FireBuilding>>();
		}
		setup();
	}

	private void init() {
		touchingDistance = new TDistance(model);
		discoveredFireBuildings = new ArrayList<FireBuilding>();
		predictedFireBuildings = new ArrayList<FireBuilding>();
		possibleFireBuildings = new HashMap<EntityID, FireBuilding>();
		discoveredFires = new HashSet<EntityID>();
		protectedFires = new HashSet<EntityID>();
		timeStep = 0;
	}

	private void fixAllExistingFires(){
		for(StandardEntity se : model.getEntitiesOfType(StandardEntityURN.BUILDING)){
			if(se instanceof Building){ // Should always be true
				Building b = (Building) se;
				if(b.isFierynessDefined() && b.getFieryness() > 0){
					System.out.println(b.getID());
					System.out.println(b.getFieryness());
					System.out.println(b.getTemperature());
					FireDiscovered(b.getID());
				}
			}
		}
	}
	public void gatherDataOnStep(){
		List<FireBuilding> allDataOnSteps = new ArrayList<FireBuilding>();
		for(StandardEntity se : model.getEntitiesOfType(StandardEntityURN.BUILDING)){
			Building b = (Building)se;
			if(b.isTemperatureDefined() && b.getTemperature() > 0){ // Building have some temperature means something have changed, since this is very sensitive to changes
				FireBuilding bd = new FireBuilding(b);
				allDataOnSteps.add(bd);
			}
		}
		practiceData.put(timeStep++, allDataOnSteps);
	}
	/**
	 * Initializes all components necessary for the prediction
	 */
	private void setup(){
		List<Integer> topology = new ArrayList<Integer>();
		topology.add(new Integer(2));
		topology.add(new Integer(3));
		topology.add(new Integer(3));
		topology.add(new Integer(3));
		topology.add(new Integer(2));
		nnFireExpansion = new NeuralNetwork(topology, 0.2);

		topology = new ArrayList<Integer>();
		topology.add(new Integer(5));
		topology.add(new Integer(2));
		nnNewFireOccurence = new NeuralNetwork(topology, 0.05);

	}
	/**
	 * Test program that learns the neural network to do an XOR-gate
	 */
	@SuppressWarnings("unused")
	private void runTestProgram(){

		// Instantiate the network
		List<Integer> topology = new ArrayList<Integer>();
		topology.add(new Integer(2));
		topology.add(new Integer(3));
		topology.add(new Integer(3));
		topology.add(new Integer(1));
		nnFireExpansion = new NeuralNetwork(topology, 0.3);

		//Start testing
		Random rand = new Random();
		List<Double> result;
		List<Double> input = new ArrayList<Double>();
		List<Double> target = new ArrayList<Double>();

		//Print the weights before simulation
		nnFireExpansion.debug();
		double error = 1;
		for(int i = 0 ; i < 1000000; ++i){
			// Clear inputs and targets
			input.clear();
			target.clear();

			// Make new inputs and target value (Simulation is for an XOR-gate)
			int nextVal = rand.nextInt(4);
			double first = (nextVal/2==1?1.0:0.0);
			double second = (nextVal%2==1?1.0:0.0);
			target.add(new Double(nextVal/2 != nextVal%2 ? 1.0:0.0));
			input.add(new Double(first));
			input.add(new Double(second));

			// Run program and receive the result
			result = nnFireExpansion.Execute(input);

			// Average the error
			error = error*0.95+Math.abs(target.get(0)-result.get(0))*0.05;

			// backpropagate the error through the net
			nnFireExpansion.BackPropagate(target);
		}

		// Print average error and the final weights
		nnFireExpansion.debug();
//		System.out.println("Error: " + error);
	}

	public void FireDiscovered(EntityID fireID){
		if(!discoveredFires.contains(fireID)){
			System.out.println("New fire discovered");
			//TODO: Handle new fire discovered
			FireBuilding newDiscovery = new FireBuilding(fireID, model, timeStep);
			discoveredFireBuildings.add(newDiscovery);
			predictedFireBuildings.add(newDiscovery);
			discoveredFires.add(fireID);
		}else{
			System.out.println("Old fire discovered");
		}
	}

	/**
	 * Function to simulate a taken timeStep in the prediction
	 */
	public void simulateStep(){
		simulateStep(predictedFireBuildings, protectedFires, possibleFireBuildings);
	}
	public void simulateStep(List<FireBuilding> predictedFB,
							 Set<EntityID> protectedFB, 
							 Map<EntityID, FireBuilding> possibleFB){
		// Increase the internally known timeStep
		++timeStep;
		List<FireBuilding> newFireBuildings = new ArrayList<FireBuilding>();
		//TODO: Handle time step prediction


		// Try to find all steps required before continuing past this point
		for(int i = 0 ; i < predictedFB.size() ; ++i){
			FireBuilding fb = predictedFB.get(i);

			// Step 0:
			// remove burnt up buildings
			if(StandardEntityConstants.Fieryness.values()[fb.getFieryness()] == StandardEntityConstants.Fieryness.BURNT_OUT){
				burntoutFireBuildings.add(predictedFB.get(i).getID());
				predictedFB.remove(i--);
				continue;
			}
			// Step 1:
			// increase the intensity of each discovered fire by a value given by the RL-algorithm
			List<Double> result = nnFireExpansion.Execute(fb.getData());
			fb.increaseIntensity(result.get(0).doubleValue());
			//fb.setFieryness(result.get(1).doubleValue());

			// Step 2:
			// Expand the search around the building to see if any new building caught on fire
			for(Entry<EntityID, Integer> entryset : touchingDistance.getDistances(fb.getBuilding()).entrySet()){
				if(entryset.getValue() > MAX_DISTANCE){
					break;
				}
				EntityID se = entryset.getKey();
				// If it's already on fire
				if(predictedFB.contains(new FireBuilding(se)))
					continue;

				if(newFireBuildings.contains(new FireBuilding(se)))
					continue;
				// If it is protected by fire brigades
				if(protectedFB.contains(se))
					continue;

				FireBuilding newFB;
				if(!possibleFB.containsKey(se)){
					newFB = new FireBuilding(se,model);
					possibleFB.put(se, newFB);
					System.out.println("New preliminary fire: " + se.toString());
				}
				else{
					newFB = possibleFB.get(se);
					System.out.println("Fetching preliminary fire: " + se.toString());
				}
				List<Double> inputs = new ArrayList<Double>(result);
				inputs.add(entryset.getValue().doubleValue());
				inputs.addAll(newFB.getData());
				List<Double> results = nnNewFireOccurence.Execute(inputs);
				if(results.get(1) > 0.0){
					newFireBuildings.add(newFB);
				}

			}
		}
		predictedFB.addAll(newFireBuildings);

	}
	public boolean protectBuilding(EntityID eID){
		if(!protectedFires.contains(eID)){
			protectedFires.add(eID);
			return true;
		}
		return false;
	}

	public boolean removeProtectBuilding(EntityID eID){
		if(protectedFires.contains(eID)){
			protectedFires.remove(eID);
			return true;
		}
		return false;
	}

	public void resetProtected(){
		protectedFires.clear();
	}
	public List<FireBuilding> getDiscoveredFires(){
		return discoveredFireBuildings;
	}
	public List<FireBuilding> getPredictedFires(int timeStep){
		//TODO: Return only the predicted fires of the specified timeStep
		return predictedFireBuildings;
	}

	public boolean startLearning() {
		if(practiceData.size() > 10){

			List<Integer> topology = new ArrayList<Integer>();
			topology.add(new Integer(2));
			topology.add(new Integer(5));
			topology.add(new Integer(2));
			System.out.println("New NN");
			nnFireExpansion.debug();
//			nnFireExpansion = new NeuralNetwork(topology, 0.2);
			double error = 1.0;
			double thisError = 0;
			Random rand = new Random();
			List<FireBuilding> current = null;
			List<FireBuilding> next = null;
			List<FireBuilding> actual = null;
			int debugRuns = 1000;
			while(/*error > 0.001*/debugRuns-- != 0){
				int rndStep = rand.nextInt(practiceData.size()-1);
				rndStep = rand.nextInt(6)+2;
				current = practiceData.get(rndStep);
				actual = practiceData.get(rndStep+1);
				next = getPrediction(current);
				backProp(current, next, actual);
//				thisError = getError(next, actual);
				if(debugRuns % 10000 == 0){
					nnFireExpansion.debug();
					System.out.println("Avg error: "+ nnFireExpansion.avgError);
					System.out.println("runsLeft: " + debugRuns);
				}

				error = (error * 0 + thisError*5)/100;
				error = 0;
//				System.out.println("error: " + error);
			}
			return true;
		}
		return false;
	}

	private double getError(List<FireBuilding> next, List<FireBuilding> actual) {
		// TODO Auto-generated method stub
		for(FireBuilding correct : actual){
			if(next.contains(correct)){
				FireBuilding assumed = next.get(next.indexOf(correct));
//				if(Math.abs(correct.temperature-assumed.temperature) > 4)
					System.out.println("fireDiff1: " + (correct.getTemperature()-assumed.getTemperature()));
			}else{
				if(correct.getTemperature() > 4)
					System.out.println("fireDiff2: " + correct.getTemperature());				
			}
		}
		for(FireBuilding assumed : next){
			if(!actual.contains(assumed)){
				if(assumed.getTemperature() > 4)
				System.out.println("fireDiff3: " + (-assumed.getTemperature()));
			}
		}
		return 0;
	}

	private void backProp(List<FireBuilding> input, List<FireBuilding> next, List<FireBuilding> actual) {
		for(FireBuilding correct : actual){
			if(next.contains(correct)){
				FireBuilding assumed = next.get(next.indexOf(correct));
				if(Math.abs(correct.getTemperature()-assumed.getTemperature()) !=0 && correct.getFieryness() == 1){
					if(input.contains(correct)){
						FireBuilding initial = input.get(input.indexOf(correct));
						System.out.println("fireDiff1: " + (correct.getTemperature()-assumed.getTemperature()));
						nnFireExpansion.Execute(input.get(input.indexOf(correct)).getData());
						List<Double> diff = new ArrayList<Double>();
						if(initial.getTemperature() != 0){
							diff.add(new Double(correct.getTemperature()*1.0/initial.getTemperature()));
						}else{
							diff.add(new Double(1));
						}
						diff.add(new Double(0));
						nnFireExpansion.BackPropagate(diff);
					}
				}
			}
//			else{
//				if(correct.temperature > 4)
//					System.out.println("fireDiff2: " + correct.temperature);				
//			}
		}
		for(FireBuilding assumed : next){
			if(!actual.contains(assumed)){
				if(assumed.getTemperature() > 4){
//					System.out.println("fireDiff3: " + (-assumed.temperature));
				}
			}
		}
	}

	private List<FireBuilding> getPrediction(List<FireBuilding> current) {
		List<FireBuilding> result = new ArrayList<FireBuilding>();


		List<FireBuilding> newFireBuildings = new ArrayList<FireBuilding>();
		for(FireBuilding bd : current){
			
			List<Double> increase = nnFireExpansion.Execute(bd.getData());
			FireBuilding newBD = new FireBuilding(bd);
			newBD.setTemperature((int)(newBD.getTemperature()*increase.get(0).doubleValue()));
			result.add(newBD);
//			if(bd.fieryness == 1)
//			System.out.println("tempIncrease: "+increase.get(0).doubleValue());
			Building b = bd.getBuilding();
			// Step 2:
			// Expand the search around the building to see if any new building caught on fire
			for(Entry<EntityID, Integer> entryset : touchingDistance.getDistances(b).entrySet()){
				if(entryset.getValue() > MAX_DISTANCE){
					break;
				}
				EntityID se = entryset.getKey();
				FireBuilding newFB = new FireBuilding(se, model);
				// If it's already on fire
				if(current.contains(newFB))
					continue;


				if(!newFireBuildings.contains(newFB)){
					newFireBuildings.add(newFB);
//					System.out.println("New preliminary fire: " + se.toString());
				}
				else{
					newFB = newFireBuildings.get(newFireBuildings.indexOf(newFB));
//					System.out.println("Fetching preliminary fire: " + se.toString());
				}
				List<Double> inputs = new ArrayList<Double>(increase);
				inputs.add(entryset.getValue().doubleValue());
				inputs.addAll(newFB.getData());
//				List<Double> results = nnNewFireOccurence.Execute(inputs);
//				newFB.temperature += results.get(0).intValue();
//				if(results.get(1) > 0.0){
//					newFB.fieryness=1;
//				}
			}
		}
		result.addAll(newFireBuildings);
		return result;
	}
}
