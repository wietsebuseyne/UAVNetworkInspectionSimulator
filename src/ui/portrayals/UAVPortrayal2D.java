package ui.portrayals;

import java.awt.Color;
import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import uav.UAV;

public class UAVPortrayal2D extends CircledPortrayal2D {

	private static final long serialVersionUID = 1L;
	
	public UAVPortrayal2D() {
		super(new OvalPortrayal2D());
	}

	@Override
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
		UAV uav = (UAV) object;
		if(uav.hasCrashed())
			paint = Color.RED;
		else
			paint = Color.GREEN;
		//set scale so that the scaled circle will have the width of twice the broadcast radius
		super.scale = uav.getBroadcastRadius()*2;
		super.draw(object, graphics, info);
    }

}
