package com.jmex.physics.util.binarymodules;

import java.io.IOException;

import com.jme.util.export.InputCapsule;
import com.jme.util.export.Savable;
import com.jme.util.export.binary.BinaryLoaderModule;
import com.jmex.physics.PhysicsCollisionGeometry;
import com.jmex.physics.PhysicsNode;
import com.jmex.physics.geometry.PhysicsSphere;

public class BinaryPhysicsSphereModule implements BinaryLoaderModule {
	public String getKey() {
        return PhysicsSphere.class.getName();
	}

	public Savable load(InputCapsule inputCapsule) throws IOException {
		PhysicsNode parent = PhysicsCollisionGeometry.readPhysicsNodeFromInputCapsule( inputCapsule );
		return parent.createSphere( null );
	}
}
