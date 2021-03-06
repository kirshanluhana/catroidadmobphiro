/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2017 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.test.physics.actions;

import android.util.Log;

import com.badlogic.gdx.scenes.scene2d.Action;

import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.content.ActionFactory;
import org.catrobat.catroid.content.CollisionScript;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.PlaceAtBrick;
import org.catrobat.catroid.physics.PhysicsCollision;
import org.catrobat.catroid.physics.PhysicsObject;
import org.catrobat.catroid.physics.PhysicsWorld;
import org.catrobat.catroid.physics.content.actions.IfOnEdgeBouncePhysicsAction;
import org.catrobat.catroid.test.physics.PhysicsBaseTest;
import org.catrobat.catroid.test.utils.Reflection;
import org.catrobat.catroid.test.utils.TestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IfOnEdgeBouncePhysicsActionTest extends PhysicsBaseTest {

	private static final String TAG = IfOnEdgeBouncePhysicsActionTest.class.getSimpleName();

	private void logState(String explanationText, Sprite sprite, PhysicsObject physicsObject) {
		Log.d(TAG, explanationText);
		Log.d(TAG, "getX():" + sprite.look.getXInUserInterfaceDimensionUnit());
		Log.d(TAG, "getY():" + sprite.look.getYInUserInterfaceDimensionUnit());
		Log.d(TAG, "getVelocity():" + physicsObject.getVelocity().x + " / " + physicsObject.getVelocity().y);
		Log.d(TAG, "-------------------------------------------------");
	}

	public void testNormalBehavior() {

		assertTrue("getLookData is null", sprite.look.getLookData() != null);

		PhysicsObject physicsObject = physicsWorld.getPhysicsObject(sprite);
		physicsObject.setType(PhysicsObject.Type.DYNAMIC);

		float setYValue = -ScreenValues.SCREEN_HEIGHT / 2 + 1; // So that nearly the half of the rectangle should be outside of the screen
		sprite.look.setYInUserInterfaceDimensionUnit(setYValue);
		float setVelocityYValue = -(IfOnEdgeBouncePhysicsAction.THRESHOLD_VELOCITY_TO_ACTIVATE_BOUNCE - 1.0f);
		physicsObject.setVelocity(physicsObject.getVelocity().x, setVelocityYValue);

		assertTrue("Unexpected Y-value", sprite.look.getYInUserInterfaceDimensionUnit() == (setYValue));

		ActionFactory factory = sprite.getActionFactory();
		Action ifOnEdgeBouncePhysicsAction = factory.createIfOnEdgeBounceAction(sprite);
		logState("Values after action creation", sprite, physicsObject);

		ifOnEdgeBouncePhysicsAction.act(0.1f);
		float setYValueAfterAct = sprite.look.getYInUserInterfaceDimensionUnit();
		logState("Values after act of the action", sprite, physicsObject);

		physicsWorld.step(0.3f);
		logState("Values after step of physics world", sprite, physicsObject);

		assertTrue(physicsObject.getY() + " >= " + setYValue, (sprite.look.getYInUserInterfaceDimensionUnit() > setYValueAfterAct));
	}

	public void testVelocityThresholdAtTopCollision() {
		assertTrue("getLookData is null", sprite.look.getLookData() != null);

		PhysicsObject physicsObject = physicsWorld.getPhysicsObject(sprite);
		physicsObject.setType(PhysicsObject.Type.DYNAMIC);

		float setYValue = ScreenValues.SCREEN_HEIGHT / 2 - 1; // So that nearly the half of the rectangle should be outside of the screen
		sprite.look.setYInUserInterfaceDimensionUnit(setYValue);
		float setVelocityYValue = IfOnEdgeBouncePhysicsAction.THRESHOLD_VELOCITY_TO_ACTIVATE_BOUNCE + 0.5f;
		physicsObject.setVelocity(physicsObject.getVelocity().x, setVelocityYValue);

		assertTrue("Unexpected Y-value", sprite.look.getY() == setYValue);
		assertTrue("Unexpected velocity-Y-value", physicsObject.getVelocity().y == setVelocityYValue);

		ActionFactory factory = sprite.getActionFactory();
		Action ifOnEdgeBouncePhysicsAction = factory.createIfOnEdgeBounceAction(sprite);
		logState("Values after action creation", sprite, physicsObject);

		ifOnEdgeBouncePhysicsAction.act(0.1f);
		logState("Values after act of the action", sprite, physicsObject);

		assertEquals("Unexpected velocity-value (expected = " + setVelocityYValue + "; actual = " + physicsObject.getVelocity().y,
				setVelocityYValue, physicsObject.getVelocity().y, TestUtils.DELTA);

		physicsWorld.step(0.3f);
		logState("Values after step of physics world", sprite, physicsObject);

		assertTrue(physicsObject.getY() + " < " + setYValue, (sprite.look.getYInUserInterfaceDimensionUnit() < setYValue));
	}

	public void testSpriteOverlapsRightAndTopAxis() {
		assertTrue("getLookData is null", sprite.look.getLookData() != null);

		PhysicsObject physicsObject = physicsWorld.getPhysicsObject(sprite);
		physicsObject.setType(PhysicsObject.Type.DYNAMIC);

		float setXValue = ScreenValues.SCREEN_WIDTH / 2 - sprite.look.getLookData().getPixmap().getWidth() / 4;
		sprite.look.setXInUserInterfaceDimensionUnit(setXValue);
		float setYValue = ScreenValues.SCREEN_HEIGHT / 2 - sprite.look.getLookData().getPixmap().getHeight() / 4;
		sprite.look.setYInUserInterfaceDimensionUnit(setYValue);

		float setVelocityXValue = 400.0f;
		float setVelocityYValue = 400.0f;
		physicsObject.setVelocity(setVelocityXValue, setVelocityYValue);

		assertTrue("Unexpected X-value", sprite.look.getX() == setXValue);
		assertTrue("Unexpected Y-value", sprite.look.getY() == setYValue);
		assertTrue("Unexpected velocity-X-value", physicsObject.getVelocity().x == setVelocityXValue);
		assertTrue("Unexpected velocity-Y-value", physicsObject.getVelocity().y == setVelocityYValue);

		ActionFactory factory = sprite.getActionFactory();
		Action ifOnEdgeBouncePhysicsAction = factory.createIfOnEdgeBounceAction(sprite);
		logState("Values after action creation", sprite, physicsObject);

		ifOnEdgeBouncePhysicsAction.act(0.1f);
		logState("Values after first act of the action", sprite, physicsObject);

		float borderX = sprite.look.getXInUserInterfaceDimensionUnit();
		float borderY = sprite.look.getYInUserInterfaceDimensionUnit();

		assertTrue(borderX + " < " + setXValue, (borderX < setXValue));
		assertTrue(borderY + " < " + setYValue, (borderY < setYValue));

		assertEquals("Unexpected velocity-X-value (expected = " + setVelocityXValue + "; actual = " + physicsObject.getVelocity().x,
				setVelocityXValue, physicsObject.getVelocity().x, TestUtils.DELTA);
		assertEquals("Unexpected velocity-Y-value (expected = " + setVelocityYValue + "; actual = " + physicsObject.getVelocity().y,
				setVelocityYValue, physicsObject.getVelocity().y, TestUtils.DELTA);

		physicsWorld.step(0.1f);
		logState("Values after first step of physics world", sprite, physicsObject);

		float prevX = sprite.look.getXInUserInterfaceDimensionUnit();
		float prevY = sprite.look.getYInUserInterfaceDimensionUnit();
		ifOnEdgeBouncePhysicsAction = factory.createIfOnEdgeBounceAction(sprite);
		ifOnEdgeBouncePhysicsAction.act(0.1f);
		assertEquals("second act should not change X-coordinate. (expected = " + prevX + "; actual = " + sprite.look.getXInUserInterfaceDimensionUnit(), prevX, sprite.look.getXInUserInterfaceDimensionUnit(), TestUtils.DELTA);
		assertEquals("second act should not change Y-coordinate. (expected = " + prevY + "; actual = " + sprite.look.getYInUserInterfaceDimensionUnit(), prevY, sprite.look.getYInUserInterfaceDimensionUnit(), TestUtils.DELTA);

		logState("Values after second act of the action", sprite, physicsObject);

		physicsWorld.step(2.3f);
		logState("Values after second step of physics world", sprite, physicsObject);

		assertTrue(sprite.look.getXInUserInterfaceDimensionUnit() + " < " + borderX + "(border value X)", (sprite.look.getXInUserInterfaceDimensionUnit() < setXValue));
		assertTrue(sprite.look.getYInUserInterfaceDimensionUnit() + " < " + borderY + "(border value Y)", (sprite.look.getYInUserInterfaceDimensionUnit() < setYValue));
	}

	public void testCollisionBroadcastOnIfOnEdgeBounce() {
		assertTrue("getLookData is null", sprite.look.getLookData() != null);

		CollisionScript spriteCollisionScript = new CollisionScript("");
		spriteCollisionScript.setAndReturnBroadcastMessage(sprite.getName(), PhysicsCollision
				.COLLISION_WITH_ANYTHING_IDENTIFIER);
		spriteCollisionScript.getScriptBrick();
		int testXValue = 300;
		int testYValue = 250;
		PlaceAtBrick testBrick = new PlaceAtBrick(testXValue, testYValue);
		spriteCollisionScript.addBrick(testBrick);
		sprite.addScript(spriteCollisionScript);

		sprite.createStartScriptActionSequenceAndPutToMap(new HashMap<String, List<String>>());

		PhysicsObject physicsObject = physicsWorld.getPhysicsObject(sprite);
		physicsObject.setType(PhysicsObject.Type.DYNAMIC);

		float setXValue = ScreenValues.SCREEN_WIDTH / 2 - sprite.look.getLookData().getPixmap().getWidth() / 4;
		sprite.look.setXInUserInterfaceDimensionUnit(setXValue);
		float setYValue = ScreenValues.SCREEN_HEIGHT / 2 - sprite.look.getLookData().getPixmap().getHeight() / 4;
		sprite.look.setYInUserInterfaceDimensionUnit(setYValue);

		assertEquals("SetXValue not the same", setXValue, sprite.look.getXInUserInterfaceDimensionUnit());
		assertEquals("setYValue not the same", setYValue, sprite.look.getYInUserInterfaceDimensionUnit());

		float setVelocityXValue = 400.0f;
		float setVelocityYValue = 400.0f;
		physicsObject.setVelocity(setVelocityXValue, setVelocityYValue);

		ActionFactory factory = sprite.getActionFactory();
		Action ifOnEdgeBouncePhysicsAction = factory.createIfOnEdgeBounceAction(sprite);

		ArrayList<Sprite> activeVerticalBounces = (ArrayList<Sprite>) Reflection.getPrivateField(PhysicsWorld.class,
				physicsWorld, "activeVerticalBounces");
		ArrayList<Sprite> activeHorizontalBounces = (ArrayList<Sprite>) Reflection.getPrivateField(PhysicsWorld.class,
				physicsWorld, "activeHorizontalBounces");

		assertTrue("Bounce Once Vertical Array is not empty", activeVerticalBounces.isEmpty());
		assertTrue("Bounce Once Horizontal Array is not empty", activeHorizontalBounces.isEmpty());

		ifOnEdgeBouncePhysicsAction.act(1.0f);

		assertFalse("Bounce Once Vertical Array is empty", activeVerticalBounces.isEmpty());
		assertFalse("Bounce Once Horizontal Array is empty", activeHorizontalBounces.isEmpty());

		assertTrue("New X value is not smaller after IfOnEdgeBounce act", setXValue > sprite.look
				.getXInUserInterfaceDimensionUnit());
		assertTrue("New Y value is not smaller after IfOnEdgeBounce act", setYValue > sprite.look
				.getYInUserInterfaceDimensionUnit());

		physicsWorld.step(2.0f);

		assertTrue("Bounce Once Vertical Array is not empty after colliding once", activeVerticalBounces.isEmpty());
		assertTrue("Bounce Once Horizontal Array is not empty after colliding once", activeHorizontalBounces.isEmpty());

		while (!allActionsOfAllSpritesAreFinished()) {
			for (Sprite spriteOfList : project.getDefaultScene().getSpriteList()) {
				spriteOfList.look.act(1.0f);
			}
		}

		assertEquals("X Value of Sprite was not set", (float) testXValue, sprite.look
				.getXInUserInterfaceDimensionUnit());
		assertEquals("Y Value of Sprite was not set", (float) testYValue, sprite.look
				.getYInUserInterfaceDimensionUnit());
	}

	public boolean allActionsOfAllSpritesAreFinished() {
		for (Sprite spriteOfList : project.getDefaultScene().getSpriteList()) {
			if (!spriteOfList.look.getAllActionsAreFinished()) {
				return false;
			}
		}
		return true;
	}
}
