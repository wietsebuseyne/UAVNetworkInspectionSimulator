package ui.control;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import simulation.UAVNetworkSimulation;

public class ControlFrame extends JFrame {
		
	private static final long serialVersionUID = 1L;
	private JButton crashButton, addUAVButton;
	
	public ControlFrame(final GUIState guistate) {
		setSize(100, 100);
		setLayout(new FlowLayout());
		
		crashButton = new JButton("Crash a UAV");
		crashButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					guistate.scheduleImmediatelyAfter(new Steppable() {
						
						private static final long serialVersionUID = 1L;

						@Override
						public void step(SimState state) {
							UAVNetworkSimulation uavNetworkSimulation = (UAVNetworkSimulation)state;
							try {
								uavNetworkSimulation.getUAVController().getRandomUAV().crash();
							} catch(IllegalStateException ex) {
								
							}
						}
					});
							
				} catch(IllegalArgumentException ex) {
					
				}

			}
		});
		add(crashButton);
		
		addUAVButton = new JButton("Add a UAV");
		addUAVButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					guistate.scheduleImmediatelyAfter(new Steppable() {
						
						private static final long serialVersionUID = 1L;

						@Override
						public void step(SimState state) {
							UAVNetworkSimulation uavNetworkSimulation = (UAVNetworkSimulation)state;
							uavNetworkSimulation.getUAVController().addUAV(uavNetworkSimulation.network.getNode(0), uavNetworkSimulation.getNavigationBehaviour());
						}
					});
							
				} catch(IllegalArgumentException ex) {
					
				}

			}
		});
		add(addUAVButton);
	}

}
