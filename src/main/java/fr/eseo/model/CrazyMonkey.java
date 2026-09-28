/*
 * 
 */
package fr.eseo.model;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// 
/**
 * The Class CrazyMonkey.
 */
public class CrazyMonkey extends Monkey{

	/** The four moves of a monkey: right, left, down, up. */
	private static final List<int[]> DIRECTIONS = Arrays.asList(
			new int[] {1, 0}, new int[] {-1, 0}, new int[] {0, 1}, new int[] {0, -1});

	/** The source of the random moves. */
	private Random random = new Random();

	/**
	 * Instantiates a new crazy monkey.
	 *
	 * @param x the coordinate x of the crazy monkey
	 * @param y the coordinate y of the crazy monkey
	 */
	public CrazyMonkey(int x, int y){
		super(Monkey.DEFAULT_SPEED, x, y);
	}
	
	/**
	 * Instantiates a new crazy monkey.
	 *
	 * @param speed the speed
	 * @param x the coordinate x of the crazy monkey
	 * @param y the coordinate y of the crazy monkey
	 */
	public CrazyMonkey(int speed, int x, int y){
		super(speed, x, y);
	}
	
	/**
	 * Instantiates a new crazy monkey with a given source of randomness (for tests).
	 *
	 * @param speed the speed
	 * @param x the coordinate x of the crazy monkey
	 * @param y the coordinate y of the crazy monkey
	 * @param random the source of the random moves
	 */
	public CrazyMonkey(int speed, int x, int y, Random random){
		super(speed, x, y);
		this.random = random;
	}
	
	/**
	 * MOVEMENT monkey, method that moves a monkey.
	 */
	public void movementMonkey(){
		// Try the four directions in a random order; stay put when boxed in.
		final List<int[]> directions = new ArrayList<int[]>(DIRECTIONS);
		Collections.shuffle(directions, this.random);
		try {
			for (int[] direction : directions) {
				try {
					this.setPositionMonk(direction[0], direction[1]);
					return;
				} catch (CollisionException e) {
					// Blocked: try the next direction.
				}
			}
		} catch (NullPointerException n) {
			this.getTimer().stop();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized(Island.LOCK){
			this.movementMonkey();
		}
	}
}
