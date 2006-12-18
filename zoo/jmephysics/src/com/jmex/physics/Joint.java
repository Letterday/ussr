/*Copyright*/
package com.jmex.physics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jme.math.Vector3f;
import com.jme.util.export.InputCapsule;
import com.jme.util.export.JMEExporter;
import com.jme.util.export.JMEImporter;
import com.jme.util.export.OutputCapsule;
import com.jme.util.export.Savable;
import com.jmex.physics.util.AssocList;

/**
 * A Joint constraints the relative location and rotation of two PhysicsNodes. By default a Joint does not allow
 * to move any relative position or rotation change between the nodes. Axes can be added to allow movement in a specific
 * direction or rotation around specific axes. Commonly a Joint can have only 6 axes. Some implementations may
 * choose to allow 3 axes for positions plus 3 different axes for rotation but commonly translation and rotation
 * axes will be equal. Commonly the axes should be perpendicular.
 *
 * @author Irrisor
 * @see com.jmex.physics.PhysicsSpace#createJoint()
 */
public abstract class Joint implements Savable {
    private static final Vector3f defaultDirection = new Vector3f();

    protected Joint() {
    }

    private final List<JointAxis> axes = new AssocList<JointAxis>( new ArrayList<JointAxis>(), new AssocList.ModificationHandler<JointAxis>() {
        public void added( JointAxis element ) {
            element.setJoint( Joint.this );
            Joint.this.added( element );
        }

        public void removed( JointAxis element ) {
            element.setJoint( null );
            Joint.this.removed( element );
        }

        public boolean canAdd( JointAxis element ) {
            return element.getJoint() != Joint.this;
        }
    } );

    protected void added( JointAxis axis ) {
    }

    public void removed( JointAxis axis ) {
    }

    /**
     * Get the list of axes to add/remove or query axes of this Joint.
     *
     * @return list of axes
     */
    public List<JointAxis> getAxes() {
        return axes;
    }

    public TranslationalJointAxis createTranslationalAxis() {
        TranslationalJointAxis axis = createTranslationalAxisImplementation();
        initAxis( axis );
        return axis;
    }

    public RotationalJointAxis createRotationalAxis() {
        RotationalJointAxis axis = createRotationalAxisImplementation();
        initAxis( axis );
        return axis;
    }

    private void initAxis( JointAxis axis ) {
        switch ( getAxes().size() ) {
            case 0:
            case 3:
                defaultDirection.set( 1, 0, 0 );
                break;
            case 1:
            case 4:
                defaultDirection.set( 0, 1, 0 );
                break;
            case 2:
            case 5:
                defaultDirection.set( 0, 0, 1 );
                break;
            default:
                defaultDirection.set( Float.NaN, Float.NaN, Float.NaN );
        }
        axis.setDirection( defaultDirection );
        getAxes().add( axis );
    }

    protected abstract TranslationalJointAxis createTranslationalAxisImplementation();

    protected abstract RotationalJointAxis createRotationalAxisImplementation();

    /**
     * @return space this joint belongs to, must not be null
     */
    public abstract PhysicsSpace getSpace();

    private boolean active;

    /**
     * @return true if joint is currently active
     */
    public final boolean isActive() {
        return active;
    }

    /**
     * Activate the node when added to a space. Deactivate when removed.
     *
     * @param value true when activated
     * @return true if node was (de)activated, false if state was already set to value
     */
    public boolean setActive( boolean value ) {
        if ( active != value ) {
            active = value;
            if ( value ) {
                getSpace().addJoint( this );
            }
            else {
                getSpace().removeJoint( this );
            }
            return true;
        }
        else {
            return false;
        }
    }


    /**
     * getter for field name
     *
     * @return current value of field name
     */
    public String getName() {
        return this.name;
    }

    /**
     * store the value for field name
     */
    private String name;

    /**
     * setter for field name
     *
     * @param value new value
     */
    public void setName( final String value ) {
        this.name = value;
    }

    /**
     * Attach this Joint to two dynamic physics nodes. All formerly attached nodes are detached.
     *
     * @param leftNode  first node this joint is attached to, not null
     * @param rightNode second node this joint is attached to, not null
     * @see #attach(com.jmex.physics.DynamicPhysicsNode)
     * @see #detach()
     */
    public abstract void attach( DynamicPhysicsNode leftNode, DynamicPhysicsNode rightNode );

    /**
     * Attach this Joint to a single dynamic physics node. This Joint then constraints the abolute translation and/or
     * rotation of the node (relative the the space).
     * All formerly attached nodes are detached.
     *
     * @param node the only node this joint is attached to, not null
     * @see #attach(DynamicPhysicsNode, DynamicPhysicsNode)
     * @see #detach()
     */
    public abstract void attach( DynamicPhysicsNode node );

    /**
     * Detach this joint from all nodes. Effectively disables the joint.
     *
     * @see #attach(DynamicPhysicsNode, DynamicPhysicsNode)
     * @see #attach(DynamicPhysicsNode)
     */
    public abstract void detach();

    /**
     * Query a list of nodes connected via this joint. This may contain 0 to 2 nodes. The list cannot be altered.
     * To alter is use the {@link #attach} and {@link #detach} methods.
     *
     * @return a list of dynamic nodes connected via this joint.
     */
    public abstract List<? extends DynamicPhysicsNode> getNodes();

    /**
     * Reset the joint as if it has just been created.
     */
    public abstract void reset();


    /**
     * Sets the anchor of this joint. The point in specified in world coordinate space if the joint attaches a single
     * node to the world. If two nodes are attached the anchor is specified in coordinate space of the first node.
     *
     * @param anchor new anchor point
     */
    public abstract void setAnchor( Vector3f anchor );

    /**
     * Query the anchor of this node. The
     * passed in Vector3f will be populated with the values, and then returned.
     * The point in specified in world coordinate space if the joint attaches a single
     * node to the world. If two nodes are attached the anchor is specified in coordinate space of the first node.
     *
     * @param store where to store the anchor (null to create a new vector)
     * @return store
     */
    public abstract Vector3f getAnchor( Vector3f store );

    /**
     * Makes the joint 'softer' as a spring. Invoke with NaN to apply default stiffness.
     *
     * @param springConstant     spring constant
     * @param dampingCoefficient damping coefficient
     * @throws UnsupportedOperationException if not supported for the current axis conficuration
     */
    public abstract void setSpring( float springConstant, float dampingCoefficient );
    
    public Class getClassTag() {
    		return Joint.class;
    }
    
    public static final String ACTIVE_PROPERTY = "active";
    public static final String AXES_PROPERTY = "axes";
    public static final String NAME_PROPERTY = "name";
    public static final String NODES_PROPERTY = "nodes";

	public void read(JMEImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule( this );
        setActive( capsule.readBoolean( ACTIVE_PROPERTY, true ) );
		setName( capsule.readString( NAME_PROPERTY, null ) );
		
		// read axes
		// axes do not have to be added here, as they're automatically
		// added by the binary modules which call create...Axis()
		capsule.readSavableArrayList( AXES_PROPERTY, null );

		// read attached nodes
		@SuppressWarnings("unchecked")
		ArrayList<DynamicPhysicsNode> nodes = capsule.readSavableArrayList( NODES_PROPERTY, null );
		if ( nodes != null ) {
			if ( nodes.size() == 1 ) {
				attach( nodes.get( 0 ) );
			}
			else if ( nodes.size() == 2 ) {
				attach( nodes.get( 0 ), nodes.get( 1 ) );
			}
		}
	}

	public void write(JMEExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule( this );
        capsule.write( isActive(), ACTIVE_PROPERTY, true );
        capsule.write( getName(), NAME_PROPERTY, null );
        capsule.writeSavableArrayList( new ArrayList<JointAxis>( axes ), AXES_PROPERTY, null );
        capsule.writeSavableArrayList( new ArrayList<DynamicPhysicsNode>( getNodes() ), NODES_PROPERTY, null );
	}
}

/*
 * $log$
 */

