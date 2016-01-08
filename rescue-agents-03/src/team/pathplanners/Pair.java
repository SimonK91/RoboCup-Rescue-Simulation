package team.pathplanners;

public class Pair<T,N> {
	public T first;
	public N second;
	public Pair(T first, N second){
		this.first = first;
		this.second = second;
	}
	

	@Override
	public String toString(){
		return "{" +first.toString() + ", " + second.toString() + "}";
	}
}
