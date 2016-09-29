package org.usfirst.frc.team3140.robot;

import org.usfirst.frc.team3140.robot.commands.RotateToAngle;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the commands and command groups that allow control of the robot.
 */
public class OI {

	public Joystick driveJoystick;
	public Button a = new JoystickButton(driveJoystick, 1);
	public Button b = new JoystickButton(driveJoystick, 2);
	public Button x = new JoystickButton(driveJoystick, 3);
	public Button y = new JoystickButton(driveJoystick, 4);
	
	public OI() {
		a.whenPressed(new RotateToAngle(180, 0.5));
		b.whenPressed(new RotateToAngle(90, 0.5));
		x.whenPressed(new RotateToAngle(270, 0.5));
		y.whenPressed(new RotateToAngle(0, 0.5));
	}
}

