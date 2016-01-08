package team.prediction.learning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Stringify {

	private static Pattern pat = Pattern.compile("([0-9]+):(.*)");
	
	public static String serialize(Object... os){
		StringBuilder sb = new StringBuilder();
		if(os.length == 1){
			String s = os[0].toString();
			return "" + s.length()  +":" + s.toString();
		}
		for(Object o : os){
			String s = o.toString();
			sb.append("" + s.length()  +":" + s.toString());
		}
		return sb.toString();
	}
	public static <T> String serializeList(Collection<T> co){
		StringBuilder sb = new StringBuilder();
		for(Object o : co){
			sb.append(serialize(o));
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Collection<String>> T parseString(String str){
		List<String> splits = new ArrayList<String>();
		Matcher match;
		match = pat.matcher(str);
		while(match.find()){
			int len = Integer.parseInt(match.group(1));
			String val = match.group(2).substring(0, len);
			String rest = match.group(2).substring(len);
			splits.add(val);
			match = pat.matcher(rest);
		}
		return (T)splits;
	}
}
