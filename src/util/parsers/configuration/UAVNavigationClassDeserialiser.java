package util.parsers.configuration;

import uav.navigation.ACONavigation;
import uav.navigation.NavigationInitializationClass;
import uav.navigation.UAVNavigationStrategy;
import uav.navigation.aco.ACOImpl;

/**
 * An adapter that can serialize and deserialize the different UAV navigation classes.
 * 
 * @author Wietse Buseyne
 *
 */
public class UAVNavigationClassDeserialiser  {
	
	@SuppressWarnings("unchecked")
	public static NavigationInitializationClass deserialize(String navName) {
		if(navName.equals("CycleNavigation"))
			throw new IllegalArgumentException("The cycle navigation cannot be parsed by this class");
		else if(navName.startsWith("ACONavigation")) {
			try {
				String[] s = navName.split("_");
				String a = s[1],
						b = s[2];
				double alpha = Double.parseDouble(a.substring(1));
				double beta = Double.parseDouble(b.substring(1));
				ACOImpl acoImpl = ACONavigation.DEFAULT_IMPL;
				if(s.length > 2) {
					acoImpl = (ACOImpl) Class.forName("uav.navigation.aco." + s[3] + "ACOImpl").newInstance();
				}
				Object[] params = new Object[3];
				params[0] = alpha;
				params[1] = beta;
				params[2] = acoImpl;
				return new NavigationInitializationClass(ACONavigation.class, ACONavigation.getParamTypes(), params);
			} catch (ClassNotFoundException | NumberFormatException | IndexOutOfBoundsException | InstantiationException | IllegalAccessException ex) {
				throw new IllegalArgumentException("Invalid ACONavigation specification. Please follow following pattern:  ACONavigation_A[number]_B[number]_[ACOImpl]"
						+ "\nThe ACOImplementation specification is optional.");
			}
		} else
			try {
				return new NavigationInitializationClass((Class<? extends UAVNavigationStrategy>) Class.forName("uav.navigation." + navName));
			} catch (ClassNotFoundException | NumberFormatException | IndexOutOfBoundsException ex) {
				throw new IllegalArgumentException("Invalid navigation strategy class name.\nDetails: " + ex.getMessage());
			}
//		String name = json.getAsString();
//		if(name.startsWith("ACONavigation")){
//			try {
//				String a = name.split("_")[1],
//						b = name.split("_")[2];
//				ACONavigation.ALPHA = Double.parseDouble(a.substring(1));
//				ACONavigation.BETA = Double.parseDouble(b.substring(1));
//				return ACONavigation.class;
//			} catch (NumberFormatException | IndexOutOfBoundsException ex) {
//				ex.printStackTrace();
//				throw new JsonParseException("Invalid ACONavigation specification. Please follow following pattern:  AcoNavigation_A[number]_B[number]");
//			}
//		}
//		try {
//			return Class.forName("uav.navigation." + name);
//		} catch (ClassNotFoundException e) {
//			throw new JsonParseException("The given class cannot be found", e);
//		}
	}

}
