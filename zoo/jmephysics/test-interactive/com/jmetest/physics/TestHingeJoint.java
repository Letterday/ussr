/*
 * Copyright (c) 2005-2006 jME Physics 2
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of 'jME Physics 2' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jmetest.physics;

import java.util.logging.Level;

import com.jme.math.Vector3f;
import com.jme.util.LoggingSystem;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.Joint;
import com.jmex.physics.JointAxis;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.geometry.PhysicsBox;
import com.jmex.physics.util.SimplePhysicsGame;

/**
 * @author ilredeldomani
 */
public class TestHingeJoint extends SimplePhysicsGame {
    protected void simpleInitGame() {
        pause = true;
        showPhysics = true;

        StaticPhysicsNode staticNode = getPhysicsSpace().createStaticNode();
        rootNode.attachChild( staticNode );
        PhysicsBox floorBox = staticNode.createBox( "floor" );
        floorBox.getLocalScale().set( 10, 0.5f, 10 );

        DynamicPhysicsNode armBase2 = getPhysicsSpace().createDynamicNode();
        rootNode.attachChild( armBase2 );
        armBase2.createBox( "armbase2" );

        armBase2.getLocalTranslation().set( 1, 2.25f, 0 );
        armBase2.getLocalScale().set( 2, 2, 2 );
        armBase2.computeMass();


        DynamicPhysicsNode armLimb1 = getPhysicsSpace().createDynamicNode();
        rootNode.attachChild( armLimb1 );
        armLimb1.createBox( "armJoint1" );
        armLimb1.getLocalScale().set( 3, 1, 1 );

        armLimb1.getLocalTranslation().set( 3.5f, 3.75f, 0 );
        armLimb1.computeMass();
        final Joint jointForLimb1 = getPhysicsSpace().createJoint();
        jointForLimb1.attach( armLimb1, armBase2 );
        jointForLimb1.setAnchor( new Vector3f( -1.5f, -0.5f, 0 ) );
        JointAxis axis = jointForLimb1.createRotationalAxis();

        axis.setDirection( new Vector3f( 0, 0, 1 ) );
    }


    public static void main( String[] args ) {
        LoggingSystem.getLogger().setLevel( Level.WARNING );
        new TestHingeJoint().start();
    }
}

/*
 * $Log: TestHingeJoint.java,v $
 * Revision 1.1  2006/12/23 22:07:01  irrisor
 * Ray added, Picking interface (natives pending), JOODE implementation added, license header added
 *
 */

