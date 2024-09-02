package nl.tudelft.simulation.xu.dsolexample;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.ModelInterface;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.AbstractDEVSModel;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.AtomicModel;
import nl.tudelft.simulation.dsol.formalisms.devs.ESDEVS.CoupledModel;
import nl.tudelft.simulation.dsol.simtime.SimTimeDouble;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.logger.Logger;


public class TransportModel extends CoupledModel implements ModelInterface.TimeDouble
{
	private static final long serialVersionUID = -3291336753821257818L;
	/** elevator */
	private Ship ship;
	/** queue */
	private FIFOQueue truckQueueAtDockA;
	/** trucks */
	private Truck[] trucks_a;
	private Truck2[] trucks_b;
	/** miner */
	private Labor labor_a;
	private Labor labor_b;
	/** queue */
	private FIFOQueue truckQueueLaborLoad;
	/** queue */
	private FIFOQueue truckQueueLaborUnload;
	private FIFOQueue truckQueueAtDockB;
	/** the number of trucks */
	private int truck_a_num = 2;
	private int truck_b_num = 2;

	public TransportModel(final String modelName, final DEVSSimulatorInterface.TimeDouble simulator)
	{
		super(modelName, simulator);
	}

	public void constructModel(SimulatorInterface<Double, Double, SimTimeDouble> simulator)
			throws SimRuntimeException, RemoteException
	{
		/** atomic components in the model */
		this.ship = new Ship("Ship", this);
		this.truckQueueAtDockA = new FIFOQueue("TruckQueueAtDockA", this);
		this.truckQueueAtDockB = new FIFOQueue("TruckQueueAtDockB", this);
		this.trucks_a = new Truck[this.truck_a_num];
		this.trucks_b = new Truck2[this.truck_b_num];
		for (int i = 0; i < this.truck_a_num; i++)
		{
			this.trucks_a[i] = new Truck("Truck_a_" + i, this);
		}
		for (int i = 0; i < this.truck_b_num; i++)
		{
			this.trucks_b[i] = new Truck2("Truck_b_" + i, this);
		}
		this.labor_a = new Labor("Labor_a", this);
		this.labor_b = new Labor("Labor_b", this);
		this.truckQueueLaborLoad = new FIFOQueue("TruckQueueLaborLoad", this);
		this.truckQueueLaborUnload = new FIFOQueue("TruckQueueLaborUnload", this);
		
		addModelComponent(this.ship);
		addModelComponent(this.truckQueueAtDockA);
		addModelComponent(this.truckQueueAtDockB);
		addModelComponent(this.truckQueueLaborLoad);
		addModelComponent(this.truckQueueLaborUnload);
		for (int i = 0; i < this.truck_a_num; i++)
		{
			addModelComponent(this.trucks_a[i]);
		}
		for (int i = 0; i < this.truck_b_num; i++)
		{
			addModelComponent(this.trucks_b[i]);
		}
		addModelComponent(this.labor_a);
		addModelComponent(this.labor_b);

		/** coupling information of the model */
		addInternalCoupling(this.ship.request_truck_a, this.truckQueueAtDockA.request);
		addInternalCoupling(this.truckQueueAtDockA.beReady, this.ship.in);
		for (int i = 0; i < this.truck_a_num; i++)
		{
			addInternalCoupling(this.ship.unload_truck, this.trucks_a[i].unload_finish);
			addInternalCoupling(this.trucks_a[i].out_to_ship, this.truckQueueAtDockA.arrival);
			addInternalCoupling(this.trucks_a[i].out_to_labor, this.truckQueueLaborLoad.arrival);
			addInternalCoupling(this.labor_a.loading, this.trucks_a[i].load_finish);
		}
		addInternalCoupling(this.ship.request_truck_b, this.truckQueueAtDockB.request);
		addInternalCoupling(this.truckQueueAtDockB.beReady, this.ship.load);
		for (int i = 0; i < this.truck_b_num; i++)
		{
			addInternalCoupling(this.ship.load_truck, this.trucks_b[i].load_finish);
			addInternalCoupling(this.trucks_b[i].out_to_labor, this.truckQueueLaborUnload.arrival);
			addInternalCoupling(this.trucks_b[i].out_to_ship, this.truckQueueAtDockB.arrival);
			addInternalCoupling(this.labor_b.loading, this.trucks_b[i].unload_finish);
		}
		addInternalCoupling(this.truckQueueLaborLoad.beReady, this.labor_a.in);
		addInternalCoupling(this.labor_a.request_truck, this.truckQueueLaborLoad.request);
		addInternalCoupling(this.labor_a.request_ship, this.ship.back_to_docka);
		addInternalCoupling(this.truckQueueLaborUnload.beReady, this.labor_b.in);
		addInternalCoupling(this.labor_b.request_truck, this.truckQueueLaborUnload.request);

		for (AbstractDEVSModel component : getModelComponents())
		{
			if (component instanceof AtomicModel)
			{
				((AtomicModel) component).initialize(0.0);
			}
		}
	}

	public void setPrintInfoFlag(boolean flag)
	{
		this.ship.setPrintInfo(flag);
		this.truckQueueAtDockA.setPrintInfo(flag);
		this.truckQueueAtDockB.setPrintInfo(flag);
		this.truckQueueLaborLoad.setPrintInfo(flag);
		this.labor_a.setPrintInfo(flag);
		for (int i = 0; i < this.truck_a_num; i++)
		{
			this.trucks_a[i].setPrintInfo(flag);
		}
		for (int i = 0; i < this.truck_b_num; i++)
		{
			this.trucks_b[i].setPrintInfo(flag);
		}
		this.truckQueueLaborUnload.setPrintInfo(flag);
		this.labor_b.setPrintInfo(flag);
	}

	public static void main(String[] args) throws NamingException, RemoteException, SimRuntimeException
	{
		Logger.setLogLevel(Level.INFO);
		/** the simulator to simulate the model */
		DEVSSimulator.TimeDouble simulator = new DEVSSimulator.TimeDouble();
		/** the model */
		TransportModel model = new TransportModel("TransportModel", simulator);
		/** start time of the simulation */
		SimTimeDouble startTime = new SimTimeDouble(0.0);
		/** warm up period (=0) */
		double warmupPeriod = 0.0;
		/** run length of the simulation */
		double runLength = 168 * 60.0;
		/** the replication */
		Replication.TimeDouble replication = new Replication.TimeDouble("replication", startTime, warmupPeriod,	runLength, model);
		/** initialize in order to construct the model (trigger constructModel method) */
		simulator.initialize(replication, ReplicationMode.TERMINATING);
		model.setPrintInfoFlag(true);
		/** start the simulation */
		simulator.start();
	}
}
