
package org.usfirst.frc.team3140.robot.subsystems;

import org.usfirst.frc.team3140.robot.commands.TeleopDrive;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 *
 */
public class ExampleSubsystem extends Subsystem {
	// BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTANTS

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTANTS

	// BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    private final SpeedController rightMotorA = new CANTalon(1);
    private final SpeedController rightMotorB = new CANTalon(2);
    private final SpeedController leftMotorA = new CANTalon(3);
    private final SpeedController leftMotorB = new CANTalon(4);
    private final RobotDrive wCDrive4 = new RobotDrive(leftMotorA, leftMotorB, rightMotorA, rightMotorB);
    private Encoder encoderRight = new Encoder(2, 3, false, EncodingType.k4X);
    private Encoder encoderLeft = new Encoder(4, 5, false, EncodingType.k4X);
    
    
    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
 
    
	private final AHRS gyro = new AHRS(Port.kMXP);

	// Parameters used for drive while running under PID Control. The values
	// not set by the controller constructor can be set by a command directly
	private double m_magnitude;

	// Instantiate the PID controller for driving in the specified direction
	private PIDController angleGyroPID = new PIDController(0.1,
	                                                       0.0, 
	                                                       0.0, 
	                                                       gyro, new AnglePIDOutput());

	private PIDController angleEncoderPID_Right = new PIDController(0.3,
                                                                    0.0005, 
                                                                    0.0, 
                                                                    encoderRight, 
			new EncoderPID_OutputRight());

	private PIDController angleEncoderPID_Left = new PIDController(0.3,
	                                                               0.0005, 
	                                                               0.0, 
	                                                               encoderLeft,
	                                                               new EncoderPID_OutputLeft());

	// Gavin Was Here

	/**
	 * Method to set the default command for the Chassis
	 */
	public void initDefaultCommand()
	{
		// BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DEFAULT_COMMAND

        setDefaultCommand(new TeleopDrive());

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DEFAULT_COMMAND
	}

	/**
	 * Method to control the drive through the specified joystick
	 */
	public void driveWithJoystick(Joystick stick)
	{
		// Drive with arcade with the Y axis for forward/backward and
		// steer with twist
		// Note: Set the sensitivity to true to decrease joystick at small input
		double twist = stick.getTwist();

		// Cube twist to decrease sensitivity
		twist = twist * twist * twist;

		// Create a dead zone for forward/backward
		double moveValue = stick.getY();
		if (moveValue < 0)
			moveValue = -1.0 * (moveValue * moveValue);
		else
			moveValue = moveValue * moveValue;

		// Drive with arcade control
		wCDrive4.arcadeDrive(moveValue, twist, false);
	}

	/**
	 * Method to control the drive in the forward/back through the specified joystick
	 * Sets the magn
	 */
	public void updateMagnitudeWithJoystick(Joystick stick)
	{
		// Drive with arcade with the Y axis for forward/backward and
		// steer with twist

		double moveValue = stick.getY();
		if (moveValue < 0)
			moveValue = moveValue * moveValue;
		else
			moveValue = -1.0 * (moveValue * moveValue);

		// Set magnitude
		setMagnitude(moveValue);
	}

	/**
	 * Method to configure the gyro based turn/drive straight PID controller
	 */
	public void configureGyroPIDs(double P, double I, double D, double minimumOutput, double maximumOutput,
			double desiredHeading, double tolerance, double power)
	{
		// update the drive power
		m_magnitude = power;

		// Reset the PID controller
		angleGyroPID.disable();
		angleGyroPID.reset();

		// Reset Encoders for when driving a given distance
		resetEncoders();

		// Set the PID gains
		angleGyroPID.setPID(P, I, D);

		// The gyro angle uses input values from 0 to 360
		angleGyroPID.setInputRange(0.0, 360.0);

		// Consider 0 and 360 to be the same point
		angleGyroPID.setContinuous(true);

		// Limit the output power when turning
		angleGyroPID.setOutputRange(minimumOutput, maximumOutput);

		// Set the PID tolerance
		angleGyroPID.setAbsoluteTolerance(tolerance);

		// Set the PID desired heading
		angleGyroPID.setSetpoint(getRelativeAngle(desiredHeading));

		// enable the PID
		angleGyroPID.enable();
	}

	/**
	 * Method to set the PID heading
	 */
	public void setGyroPID_Heading(double desiredHeading)
	{
		// Set the PID desired heading
		angleGyroPID.setSetpoint(getRelativeAngle(desiredHeading));
	}

	/**
	 * Method to get the PID target value (heading)
	 */
	public double getGyroHeadingSetpoint()
	{
		return angleGyroPID.getSetpoint();
	}

	/**
	 * Method to get the PID target value (heading)
	 */
	public double getGyroPID_Heading()
	{
		return gyro.pidGet();
	}

	/**
	 * Method to get the PID error
	 */
	public double getGyroPID_Error()
	{
		return angleGyroPID.getError();
	}

	/**
	 * Method to disable the angle PID controller
	 */
	public void disableAllPIDs()
	{
		// disable the PID
		angleGyroPID.disable();
		angleEncoderPID_Left.disable();
		angleEncoderPID_Right.disable();
	}

	/**
	 * Method to configure the encoder based turn PID controller
	 */
	public void configureEncoderPIDs(double P, double I, double D, double minimumOutput, double maximumOutput,
			double desiredEncoderValue, double tolerance)
	{
		// Reset the PID controller
		angleEncoderPID_Left.disable();
		angleEncoderPID_Right.disable();
		angleEncoderPID_Left.reset();
		angleEncoderPID_Right.reset();

		// Reset Encoder values
		resetEncoders();

		// Set the PID gains
		angleEncoderPID_Left.setPID(P, I, D);
		angleEncoderPID_Right.setPID(P, I, D);

		// Set the encoder input value range
		angleEncoderPID_Left.setInputRange(-1000, 1000);
		angleEncoderPID_Right.setInputRange(-1000, -1000);

		// Set the encoder output range
		angleEncoderPID_Left.setOutputRange(-0.4, 0.4);
		angleEncoderPID_Right.setOutputRange(-0.4, -0.4);
		// Set the PID tolerance
		angleEncoderPID_Left.setAbsoluteTolerance(tolerance);
		angleEncoderPID_Right.setAbsoluteTolerance(tolerance);

		// Set the PID desired set point
		angleEncoderPID_Left.setSetpoint(-desiredEncoderValue);
		angleEncoderPID_Right.setSetpoint(desiredEncoderValue);

		// enable the PID
		angleEncoderPID_Left.enable();
		angleEncoderPID_Right.enable();
	}

	/**
	 * Method to set the PID set point
	 */
	public void setEncoderPID_Setpoint(double desiredEncoderValue)
	{
		// Set the PID desired set point
		angleEncoderPID_Left.setSetpoint(-desiredEncoderValue);
		angleEncoderPID_Right.setSetpoint(desiredEncoderValue);
	}

	/**
	 * Method to get the PID target value (heading)
	 */
	public double getEncoderSetpoint()
	{
		// Return the Left set point
		// Note: The Right set point should just be the negative of the Left
		return angleEncoderPID_Left.getSetpoint();
	}

	/**
	 * Method to get the PID error
	 */
	public double getEncoderPID_Error()
	{
		// Return the Left PID error
		return angleEncoderPID_Left.getError();
	}

	/**
	 * Method to stop the chassis drive motors
	 */
	public void stop()
	{
		// Stop all motors
		wCDrive4.arcadeDrive(0, 0);

		// Disable PID Controller
		this.disableAllPIDs();
	}

	/**
	 * Method to get larger of the encoder distances
	 */
	public double getDistance()
	{
		// Return the maximum encoder distance in case the other is not working
		return (Math.max(encoderLeft.getDistance(), encoderRight.getDistance()));
	}

	/**
	 * Method to reset both encoders
	 */
	public void resetEncoders()
	{
		encoderLeft.reset();
		encoderRight.reset();
	}

	/**
	 * Method to return the present gyro angle
	 */
	public double getCurrentHeading()
	{
		// Return the relative gyro angle
		return (getRelativeAngle(gyro.getAngle()));
	}

	/**
	 * Method to return a relative gyro angle (between 0 and 360)
	 */
	private double getRelativeAngle(double angle)
	{
		// Adjust the angle if negative
		while (angle < 0.0)
			angle += 360.0;

		// Adjust the angle if greater than 360
		while (angle >= 360.0)
			angle -= 360.0;

		// Return the angle between 0 and 360
		return angle;
	}

	/**
	 * Method to reset the chassis gyro
	 *
	 * Note: Should only be called once just before the autonomous command starts
	 */
	public void resetGyro()
	{
		// Reset the gyro (angle goes to zero)
		gyro.reset();
	}

	/**
	 * Method to set the desired chassis speed (magnitude) for PID controlled
	 * moves. Only to be used while controlled by PID controller
	 */
	public void setMagnitude(double magnitude)
	{
		m_magnitude = magnitude;
	}

	/**
	 * Set the chassis drive motor
	 */
	public void setWheelOutput(double rightWheel, double leftWheel)
	{
		// Set the right motors for forward direction
		rightMotorA.set(rightWheel);
		rightMotorB.set(rightWheel);

		// Set the Left motors for forward direction
		// Note: The Left motors are opposite the Right motors
		leftMotorA.set(-leftWheel);
		leftMotorB.set(-leftWheel);
	}

	/**
	 * Get the Right encoder value
	 */
	public int getRightEncoderValue()
	{
		return encoderRight.get();
	}

	/**
	 * Get the Left encoder value
	 */
	public int getLeftEncoderValue()
	{
		return encoderLeft.get();
	}

	/**
	 * Return the right encoder distance
	 */
	public double getRightDistance()
	{
		return (encoderRight.getDistance());
	}

	/**
	 * Return the left encoder distance
	 */
	public double getLeftDistance()
	{
		return (encoderLeft.getDistance());
	}

	/**
	 * Class declaration for the PIDOutput
	 */
	public class AnglePIDOutput implements PIDOutput
	{
		/**
		 * Virtual function to receive the PID output and set the drive direction 
		 */
		public void pidWrite(double PIDoutput)
		{
			// Drive the robot given the speed and direction
			// Note: The Arcade drive expects a joystick which is negative forward)
			wCDrive4.arcadeDrive(-m_magnitude, PIDoutput);
		}
	}

	/**
	 * Class declaration for the PIDOutput
	 */
	public class EncoderPID_OutputRight implements PIDOutput
	{
		/**
		 * Virtual function to receive the PID output and set the drive direction 
		 */
		public void pidWrite(double PIDoutput)
		{
//			SmartDashboard.putNumber("SpeedControllerPIDOutputRight: ", PIDoutput);

			rightMotorA.set(PIDoutput);
			rightMotorB.set(PIDoutput);
		}
	}

	/**
	 * Class declaration for the PIDOutput
	 */
	public class EncoderPID_OutputLeft implements PIDOutput
	{
		/**
		 * Virtual function to receive the PID output and set the drive direction 
		 */
		public void pidWrite(double PIDoutput)
		{
//			SmartDashboard.putNumber("SpeedControllerPIDOutputLeft: ", -PIDoutput);

			leftMotorA.set(-PIDoutput);
			leftMotorB.set(-PIDoutput);
		}
	}
}
