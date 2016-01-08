package team.communicationstrategies;

public enum CommunicationProtocol {


		
		FOUND_CIVILIAN(0), FOUND_FIRE(1) , FOUND_BLOCKADE(2), FOUND_BURIED_CIVILIAN(3), FOUND_HELPLESS_CIVILIAN(4), FOUND_DYING_CIVILIAN (5),
		
		EXTINGUISHING_FIRE(6), CLEARING_BLOCKADE(7),
		
		GO_RESCUE_CIVILIAN(8), GO_EXTINGUISH_FIRE(9), GO_CLEAR_BLOCKADE(10),
		
		FAULTY_MESSAGE(11), GOT_NOTHING_TO_DO(12), I_AM_BURIED(13), GO_RESCUE_BURIED(14), BLOCKED_BY_BLOCKADE(15), RESCUED_BURIED(16);
		
	    private final int shortVal;
	    private CommunicationProtocol(int shortVal) 
	    {
	        this.shortVal = shortVal;
	    }

	    public String Short() 
	    {
	        return Integer.toString(shortVal);
	    }
		
	    public static CommunicationProtocol GetProtocolType(String str)
	    {
	    	try
	    	{
	    		int i = Integer.parseInt(str);
	    		if(i >= CommunicationProtocol.values().length || i < 0)
	    			return CommunicationProtocol.FAULTY_MESSAGE;
	    		for(CommunicationProtocol a : CommunicationProtocol.values())
	    		{
	    			if(a.shortVal == i)
	    				return a;
	    		}
	    		return FAULTY_MESSAGE;
	    	}
	    	catch(NumberFormatException e)
	    	{
	    		return FAULTY_MESSAGE;
	    	}
	    }
	    
}
