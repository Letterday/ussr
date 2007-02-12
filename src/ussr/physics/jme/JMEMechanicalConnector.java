package ussr.physics.jme;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ussr.model.Connector;
import ussr.physics.PhysicsLogger;
import ussr.robotbuildingblocks.GeometryDescription;
import ussr.robotbuildingblocks.RobotDescription;

import com.jme.math.Vector3f;
import com.jme.scene.TriMesh;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.Joint;
import com.jmex.physics.RotationalJointAxis;

public class JMEMechanicalConnector implements JMEConnector {
    /**
     * The abstract connector represented by this jme entity
     */
    private Connector model;
    private DynamicPhysicsNode node;
    private JMESimulation world;
    private JMEConnector connectedConnector = null;
    private JMEMechanicalConnector lastProximityConnector = null;
    private JMEModuleComponent module;
    private float maxConnectDistance;
    private TriMesh mesh;

    public JMEMechanicalConnector(Vector3f position, DynamicPhysicsNode moduleNode, String baseName, JMESimulation world, JMEModuleComponent component, RobotDescription selfDesc) {
        List<GeometryDescription> geometry = selfDesc.getConnectorGeometry();
        this.world = world;
        this.module = component;
        this.maxConnectDistance = selfDesc.getMaxConnectionDistance();
        // Create connector node
        DynamicPhysicsNode connector = moduleNode;
        // Create visual appearance
        assert geometry.size()==1; // Only tested with size 1 geometry
        for(GeometryDescription element: geometry) {
        	//System.out.println("creating connector "+(baseName+position.toString()));
        	mesh = JMEDescriptionHelper.createShape(connector, baseName+position.toString(), element);
            //world.connectorRegistry.put(mesh.getName(),this);
            mesh.getLocalTranslation().set( mesh.getLocalTranslation().add(position) );
            
            //mesh.getLocalTranslation().
            //Quaternion q = module.getModuleNode().getLocalRotation();
            //System.out.println("rotation="+component.getComponentGeometries().get(0).getLocalRotation());
            //mesh.getLocalRotation().set();
            //mesh.setModelBound( new BoundingSphere() );
            //mesh.updateModelBound();
            
            connector.attachChild( mesh );
            component.getComponentGeometries().add(mesh);
            world.associateGeometry(connector, mesh);
            JMEDescriptionHelper.setColor(world, mesh, element.getColor());
        }
        // Finalize
        //connector.generatePhysicsGeometry(true); //dont let connectors collide - too slow!
        world.getRootNode().attachChild( connector );
        //connector.computeMass();
        this.node = connector;
    }
    
    /* (non-Javadoc)
     * @see ussr.physics.jme.JMEConnector#getNode()
     */
    public DynamicPhysicsNode getNode() { return node; }
    

    /* (non-Javadoc)
     * @see ussr.physics.jme.JMEConnector#otherConnectorAvailable()
     */
    public boolean hasProximateConnector() {
        return this.lastProximityConnector!=null;
    }
    
    /* (non-Javadoc)
     * @see ussr.physics.jme.JMEConnector#getAvailableConnectors()
     */
    public synchronized List<Connector> getAvailableConnectors() {
        if(this.lastProximityConnector==null) return Collections.emptyList();
        if(node.getLocalTranslation().distance(lastProximityConnector.node.getLocalTranslation())>maxConnectDistance) {
            lastProximityConnector = null;
            return Collections.emptyList();
        }
        return Arrays.asList(new Connector[] { this.lastProximityConnector.model });
    }
    
    /* (non-Javadoc)
     * @see ussr.physics.jme.JMEConnector#isConnected()
     */
    public synchronized boolean isConnected() {
        return connectedConnector!=null;
    }

    /* (non-Javadoc)
     * @see ussr.physics.jme.JMEConnector#connectTo(ussr.physics.PhysicsConnector)
     */
    public synchronized boolean connect() {
        throw new Error("Should not call connect() on mechanical connector");
    }

    /* (non-Javadoc)
     * @see ussr.physics.jme.JMEConnector#reset()
     */
    public void reset() {
        connectedConnector = null;        
    }
    
    /* (non-Javadoc)
     * @see ussr.physics.jme.JMEConnector#toString()
     */
    public String toString() {
        return "JMEConnector<"+node.hashCode()+">";
    }

    /* (non-Javadoc)
     * @see ussr.physics.jme.JMEConnector#setModel(ussr.model.Connector)
     */
    public void setModel(Connector connector) {
        this.model = connector;        
    }
    private Joint connection;
    public void connectTo(JMEMechanicalConnector jc2) {
    	/*System.out.println("Connecter me "+toString()+"to "+jc2.toString());
        world.getRootNode().unlockMeshes();
        DynamicPhysicsNode adopter = jc2.getNode();
        Vector3f nodeDistance = adopter.getLocalTranslation().subtract(this.node.getLocalTranslation());
        Matrix3f adopterRotation = adopter.getLocalRotation().toRotationMatrix();
        Matrix3f localRotation = node.getLocalRotation().toRotationMatrix();
        Matrix3f nodeRotation = localRotation.invert().mult(adopterRotation);
        for(TriMesh element: world.getNodeGeometries(this.node)) {
            adopter.attachChild(element);
            element.setLocalTranslation(element.getLocalTranslation().add(nodeDistance));
            element.setLocalRotation(nodeRotation.mult(element.getLocalRotation().toRotationMatrix()));
            //JMEDescriptionHelper.setColor(world, element, Color.CYAN);
            
            //element.setModelBound( new BoundingSphere() );
            //element.updateModelBound();
        }
        adopter.generatePhysicsGeometry();
        adopter.computeMass();*/
    	if(this.connectedConnector==null&& jc2.connectedConnector==null) {
	    	connection = world.getPhysicsSpace().createJoint();
	    	connection.attach(getNode(),jc2.getNode());
	    	connection.setAnchor(node.getLocalRotation().mult(mesh.getLocalTranslation()));
	    	
	    	//RotationalJointAxis xAxis = connection.createRotationalAxis(); //xAxis.setDirection(new Vector3f(1,0,0));
	    	//RotationalJointAxis yAxis = connection.createRotationalAxis(); //yAxis.setDirection(new Vector3f(0,1,0));
	    	//RotationalJointAxis zAxis = connection.createRotationalAxis(); //zAxis.setDirection(new Vector3f(0,0,1));
	    	//xAxis.setAvailableAcceleration(1000);
	    	//yAxis.setAvailableAcceleration(1000);
	    	//zAxis.setAvailableAcceleration(1000);
	    	
	    	//xAxis.setPositionMaximum(0); yAxis.setPositionMinimum(0);
	    	//yAxis.setPositionMaximum(0); yAxis.setPositionMinimum(0);
	    	//zAxis.setPositionMaximum(0); zAxis.setPositionMinimum(0);
	    	
	    	/*xAxis.setPositionMaximum((float)Math.PI/6); yAxis.setPositionMinimum((float)-Math.PI/6);
	    	yAxis.setPositionMaximum((float)Math.PI/6); yAxis.setPositionMinimum((float)-Math.PI/6);
	    	zAxis.setPositionMaximum((float)Math.PI/6); zAxis.setPositionMinimum((float)-Math.PI/6);*/
	    	
	    	//joint.setSpring(100000, 10);
	        this.connectedConnector = jc2;
	        jc2.connectedConnector = this;
    	}
    	else {
    		System.out.println("Already connected");
    	}
    }
    
    public void disconnect() {
        PhysicsLogger.log("WARNING: broken method");
        node.unlockMeshes();
        node.unlockBounds();
        if(connectedConnector==null) return;
        DynamicPhysicsNode newNode = world.getPhysicsSpace().createDynamicNode();
        newNode.setLocalTranslation(node.getLocalTranslation());
        newNode.setLocalRotation(node.getLocalRotation());
        for(TriMesh element: module.getComponentGeometries()) {
            element.removeFromParent();
            newNode.attachChild(JMEDescriptionHelper.cloneElement(element));
        }
        newNode.generatePhysicsGeometry();
        newNode.computeMass();
        world.getRootNode().attachChild(newNode);
        node.updateModelBound();
    }

	public void setConnectorColor(Color color) {
//		JMEDescriptionHelper.setColor(world, mesh, color);
		/*System.out.println("Setting color for children");
    	for(Spatial child:  node.getChildren()) {
    		System.out.println("Here "+child);
    		JMEDescriptionHelper.setColor(world, child, color);        	
    	}
    	System.out.println("Done setting color for children");*/
		
	}
	/**
	 * Position relative to module
	 * @return position
	 */
	public Vector3f getPos() {
		Vector3f pos = node.getLocalRotation().mult(mesh.getLocalTranslation()).add(node.getLocalTranslation());
		return pos;
	}
}
