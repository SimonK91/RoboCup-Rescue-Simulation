package team.prediction.learning;

import java.util.List;

public abstract class Saveable {

	protected Saveable(){};
	public Saveable(String str){
		loadFromString((List<String>)Stringify.parseString(str));
	}
	abstract protected void loadFromString(List<String> str);
}
