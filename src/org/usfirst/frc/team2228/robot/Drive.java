package org.usfirst.frc.team2228.robot;

//Carrying over the classes from other libraries
import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc.team2228.robot.ConstantMap.AutoChoices;

public class Drive
{
	private RobotDrive driveStyle;
	private Joystick joystick1;
	private Joystick joystick2;
	private CANTalon right1;
	private CANTalon left1;
	private CANTalon right2;
	private CANTalon left2;
	private boolean newButtonValue = false;
	private boolean oldButtonValue = false;
	private boolean driveType = false;
	private double startTime;
	private double gearValue;
	private boolean pressed;
	private Gyro gyro;
	private double currentAngle;
	private int counter;

	private Accelerometer accel;
	private double accelerationI;
	private double accelerationF;
	private double velocityI;
	private double velocityF;
	private double position;

	final double timeoutValue = 1.5; // seconds

	public enum Goal
	{
		DO_NOTHING, BASE_LINE_TIME,

	}

	public Goal autoGoal;

	public enum State
	{
		INIT, WAIT_FOR_TIME, DONE
	}

	public State state;

	// Constructor
	public Drive()
	{
		// "Creating" the objects
		// Create the four motor controller objects for the drive base
		right1 = new CANTalon(RobotMap.RIGHT_ONE_DRIVE); // 2
		right2 = new CANTalon(RobotMap.RIGHT_TWO_DRIVE); // 1
		left1 = new CANTalon(RobotMap.LEFT_ONE_DRIVE); // 3
		left2 = new CANTalon(RobotMap.LEFT_TWO_DRIVE); // 4
		// Creating the joystick objects
		gearValue = .1;
		pressed = false;
		// creating a gear system
		joystick1 = new Joystick(0);
		joystick2 = new Joystick(1);
		// Create the RobotDrive object
		driveStyle = new RobotDrive(right1, left1);
		driveStyle.tankDrive(joystick1, joystick2);
		// Set left2 and right2 to follow the commands of left1 and left2
		right2.changeControlMode(TalonControlMode.Follower);
		right2.enableControl();
		right2.set(right1.getDeviceID());
		left2.changeControlMode(TalonControlMode.Follower);
		left2.enableControl();
		left2.set(left1.getDeviceID());
		autoGoal = Goal.DO_NOTHING;
		state = State.INIT;

		gyro = new AnalogGyro(0);
		gyro.calibrate();
		currentAngle = 0;
		counter = 0;

		// accelerometer
		accel = new BuiltInAccelerometer(Accelerometer.Range.k4G);
		accelerationI = 0;
		accelerationF = 0;
		velocityI = 0;
		velocityF = 0;
		position = 0;

		SmartDashboard.putNumber("gyroAngle", gearValue);
		SmartDashboard.putNumber("Acceleration", 0);
		SmartDashboard.putNumber("Velocity", 0);
		SmartDashboard.putNumber("Position", 0);

	}

	// Called once at the beginning of the autonomous period
	public void autonomousInit(AutoChoices autoSelected)
	{
		System.out.println("We are in AutoInit");
		switch (autoSelected)
		{

			case DO_NOTHING:
				System.out.println("Do Nothing");
				autoGoal = Goal.DO_NOTHING;
				break;
			case BASE_LINE_TIME:
				System.out.println("Now driving to Base Line only");
				autoGoal = Goal.BASE_LINE_TIME;
				state = State.INIT;
				break;
			default:

				break;
		}
	}

	// Called continuously during the autonomous period
	public void autonomousPeriodic()
	{
		switch (autoGoal)
		{
			case DO_NOTHING:
				break;
			case BASE_LINE_TIME:
				if (state == State.INIT)
				{
					driveStyle.arcadeDrive(0.5, 0, false);
					state = State.WAIT_FOR_TIME;
					startTime = Timer.getFPGATimestamp();
					System.out.println("Start:");
					System.out.println(startTime);
					startTime += timeoutValue;
					System.out.println("end:");
					System.out.println(startTime);
				}
				else if (state == State.WAIT_FOR_TIME)
				{
					if (Timer.getFPGATimestamp() >= startTime)

					{
						right1.set(0);
						left1.set(0);
						state = State.DONE;
						System.out.println("dun!");
						System.out.println(Timer.getFPGATimestamp());
					}
				}
				else
				{

				}
				break;
			default:
				System.out.println("at default");

		}

	}
	// SAVE MEEEEEEEEEE

	// Called continuously during the teleop period
	public void teleopPeriodic()
	{

		// Press a button (7) to enter "chessyDrive" otherwise drive in
		// "tankDrive"
		// driveStyle.tankDrive(joystick1, joystick2);
		if (gearValue > 1)
		{
			// HELP
			gearValue = 1;

		}
		else if (gearValue < 0.4)
		{

			gearValue = .4;


		else if (joystick2.getRawButton(RobotMap.JOY2_BUTTON_8_INCREASE_SPEED)

				&& !pressed)
		{

			gearValue += .3;
			pressed = true;

		}

		else if (joystick2.getRawButton(RobotMap.JOY2_BUTTON_7_DECREASE_SPEED)

				&& !pressed)
		{

			gearValue -= .3;
			pressed = true;
			// PLEASE
		}

		else if (!(joystick2.getRawButton(RobotMap.JOY2_BUTTON_8_INCREASE_SPEED)
				|| joystick2.getRawButton(7)))

		{
			pressed = false;
		}

		changeDriveStyle();
		if (driveType == false)
		{
			// driveStyle.arcadeDrive(joystick2, 1, joystick1, 0);
			chessyDrive(joystick2, 1, joystick1, 0);
			SmartDashboard.putString("Driving Mode", "ArcadeDrive");
		}
		else
		{
			driveStyle.tankDrive(joystick1, joystick2);

			SmartDashboard.putString("Driving Mode", "TankDrive");
		}

		// acceleration code

		accelerationF = accel.getZ();

		if (accelerationF > 0.05)
		{

			if (accelerationI > accelerationF)
			{

				velocityF = (accelerationI
						+ ((accelerationF - accelerationI) / 2)) * .02
						+ velocityF;

			}
			else if (accelerationI < accelerationF)
			{

				velocityF = (accelerationF
						+ ((accelerationI - accelerationF) / 2)) * .02
						+ velocityF;

			}

		}
		else if (accelerationF < -0.05)
		{

			if (accelerationI > accelerationF)
			{

				velocityF = (accelerationF
						+ ((accelerationI - accelerationF) / 2)) * .02
						+ velocityF;

			}
			else if (accelerationI < accelerationF)
			{

				velocityF = (accelerationI
						+ ((accelerationF - accelerationI) / 2)) * .02
						+ velocityF;

			}

		}
		else
		{
			accelerationF = 0;
		}

		if (velocityF < 0.01 && velocityF > -0.01)
		{
			velocityF = 0;
		}

		accelerationI = accelerationF;
		SmartDashboard.putNumber("Acceleration", accelerationF);
		SmartDashboard.putNumber("Velocity", velocityF);

	}

	public void testPeriodic()
	{

	}

	public Joystick getJoystick()
	{
		return joystick2;
	}

	public void chessyDrive(Joystick joys1, int axis1, Joystick joys2,
			int axis2)
	{

		// double moveValue = joys1.getRawAxis(axis1);
		// double rotateValue = -1 * joys2.getRawAxis(axis2);
		double moveValue = (joys1.getRawAxis(1) * gearValue);
		double rotateValue = (joys1.getRawAxis(2) * -1) * gearValue;


		// if(rotateValue < 0.1 && rotateValue > -0.1){
		//
		// if(gyro.getAngle()>3){
		//
		// rotateValue-=.3;
		//
		// }else if(gyro.getAngle()<-3){
		//
		// rotateValue+=.3;
		//
		// }
		//
		// }


		driveStyle.arcadeDrive(moveValue, rotateValue, false);

	}

	private void changeDriveStyle()
	{
		newButtonValue = joystick2
				.getRawButton(RobotMap.JOY2_BUTTON_1_DRIVE_TYPE_SWITCH);

		if (newButtonValue != oldButtonValue)
		{
			if (newButtonValue == true)
			{
				if (driveType == false)
				{
					// driveStyle.arcadeDrive(joystick2, 1, joystick1, 0);
					driveType = true;
				}
				else
				{
					// driveStyle.tankDrive(joystick1, joystick2);
					driveType = false;
				}

			}
			oldButtonValue = newButtonValue;
		}
	}
}