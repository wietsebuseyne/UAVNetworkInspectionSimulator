package ui.portrayals;

import java.awt.Color;
import java.awt.Graphics2D;

import network.Node;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;

public class NodePortrayal2D extends OvalPortrayal2D {

	private static final long serialVersionUID = 1L;

	@Override
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
		Node node = (Node) object;
		if(node.isRechargeNode())
			paint = Color.ORANGE;
		else if(node.isRechargeNode())
			paint = Color.BLUE;
		else
			paint = Color.GRAY;
		super.draw(object, graphics, info);
	}

}
