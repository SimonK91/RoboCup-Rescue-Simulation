package team.communicationstrategies;

import rescuecore2.standard.entities.StandardEntityURN;

public enum EntityURNProtocol {
	ROAD(1), BUILDING(2), CIVILIAN(3), BLOCKADE(4);
	
	
	
	
    private final int shortVal;
    private EntityURNProtocol(int shortVal) {
        this.shortVal = shortVal;
    }
	
	public static String GetStringValue(String e) {
		StandardEntityURN urn = StandardEntityURN.fromString(e);

		switch(urn)
		{
		case ROAD:
			return "1";
		case BUILDING:
			return "2";
		case CIVILIAN:
			return "3";
		case BLOCKADE:
			return "4";
		default:
			break;
		}
		return null;
    }
	
    public static StandardEntityURN GetEntityURN(int i){
    	switch(i)
    	{
    	case 1:
    		return StandardEntityURN.ROAD;
    	case 2:
    		return StandardEntityURN.BUILDING;
    	case 3:
    		return StandardEntityURN.CIVILIAN;
    	case 4:
    		return StandardEntityURN.BLOCKADE;
    	}
		return null;
    }

}
