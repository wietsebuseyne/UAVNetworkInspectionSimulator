#UAV Network Simulation
This software allows to perform simulations of using multiple UAVs in a network and compare different configurations.
#Libraries
The following libraries are used in this project:
* algs4 (http://algs4.cs.princeton.edu/home/)
* commons-cli-1.3.1 (https://commons.apache.org/proper/commons-cli/)
* gson-2.4 (https://github.com/google/gson)
* jcommon-1.0.23 (http://www.jfree.org/jcommon/)
* jfreechart-1.0.19 (http://www.jfree.org/jfreechart)/
* mason.19 (http://cs.gmu.edu/~eclab/projects/mason/)
# Operation
By default, the simulator will run the experiments in the experiments.json file with the configuration.json as parameters and store the output files in folders with the names of the experiments followed by the date and time of execution. To start in gui mode, use the `-g` or `--gui` switch. The navigation strategy to be used in GUI-mode can be specified by using the `-n` or `--nav` switch followed by the navigation strategy. The `-u` or `--uavs` switch followed by a number sets the number of UAVs to be used in GUI-mode.
