package uav.navigation;

import java.lang.reflect.InvocationTargetException;

public class NavigationInitializationClass {
	
	private Class<?>[] paramTypes = new Class<?>[0];
	private Object[] params = new Object[0];
	private Class<? extends UAVNavigationStrategy> navigationBehaviour;
	
	public NavigationInitializationClass(Class<? extends UAVNavigationStrategy> navigationBehaviour) {
		this.navigationBehaviour = navigationBehaviour;
	}

	public NavigationInitializationClass(Class<? extends UAVNavigationStrategy> navigationBehaviour,
			Class<?>[] paramTypes,
			Object[] params) {
		if(paramTypes.length != params.length)
			throw new IllegalArgumentException("The number of parameter types must be equal to the number of parameters");
		this.paramTypes = paramTypes;
		this.params = params;
		this.navigationBehaviour = navigationBehaviour;
	}
	
	public UAVNavigationStrategy newInstance() {
		try {
			return navigationBehaviour.getConstructor(paramTypes).newInstance(params);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("The navigation behaviour could not be initialized. \nDetails: " + e.getMessage());
		}
	}

	public String getSimpleName() {
		return navigationBehaviour.getSimpleName();
	}

	public Class<? extends UAVNavigationStrategy> getNavigationBehaviour() {
		return navigationBehaviour;
	}

}
