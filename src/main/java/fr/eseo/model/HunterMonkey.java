package fr.eseo.model;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

//
/**
 * The Class HunterMonkey.
 */
public class HunterMonkey extends Monkey{

	/**
	 * Instantiates a new HunterMonkey.
	 * @param x the coordinate x of the monkey
	 * @param y the coordinate y of the monkey
	 */
	public HunterMonkey( int x, int y){
		super( x, y);
	}
	/**
	 * Instantiates a new hunter monkey.
	 *
	 * @param speed the speed
	 * @param x the coordinate x of the monkey
	 * @param y the coordinate y of the monkey
	 */
	public HunterMonkey(int speed, int x, int y){
		super(speed, x, y);
	}

	/**
	 * Get the closest living pirate of the monkey (Manhattan distance).
	 * @param pirates the list of pirates of the island
	 * @return the closest living pirate, or null if every pirate is dead
	 */
	public Pirate closerPirate(ArrayList<Pirate> pirates){
		Pirate closest = null;
		int bestDistance = Integer.MAX_VALUE;
		for(Pirate pirate : pirates){
			if(pirate.getState() != StatePirate.dead){
				final int distance = Math.abs(pirate.getCoordinateX() - this.getCoordinateX())
						+ Math.abs(pirate.getCoordinateY() - this.getCoordinateY());
				if(distance < bestDistance){
					bestDistance = distance;
					closest = pirate;
				}
			}
		}
		return closest;
	}

	/**
	 * Hunt a pirate: one step toward it, along the axis where it is the farthest.
	 * @param pirate the pirate
	 * @return tab the movement (0,1 or 0,-1 or -1,0 or 1,0, or 0,0 on the pirate)
	 */
	public int[] huntingPirates(Pirate pirate){
		final int[] tab = {0, 0};
		final int dx = pirate.getCoordinateX() - this.getCoordinateX();
		final int dy = pirate.getCoordinateY() - this.getCoordinateY();
		if(Math.abs(dx) >= Math.abs(dy)){
			tab[0] = Integer.signum(dx);
		}else{
			tab[1] = Integer.signum(dy);
		}
		return tab;
	}


	/**
	 * Move a hunter monkey.
	 * @param tab the movement (0,1 or 0,-1 or -1,0 or 1,0)
	 * @throws CollisionException collision with an entity
	 */
	public void movementHunterMonkey(int[] tab) throws CollisionException{
		this.setPositionMonk(tab[0], tab[1]);
	}

	/**
	 * Chase the closest pirate. When the direct path is blocked, try the other axis.
	 */
	public void chase(){
		final Pirate target = this.closerPirate(Island.getInstance().getPirates());
		if(target == null){
			return;
		}
		final int[] step = this.huntingPirates(target);
		try{
			this.movementHunterMonkey(step);
		}catch(CollisionException blocked){
			final int[] detour = {0, 0};
			if(step[0] != 0){
				detour[1] = Integer.signum(target.getCoordinateY() - this.getCoordinateY());
			}else{
				detour[0] = Integer.signum(target.getCoordinateX() - this.getCoordinateX());
			}
			try{
				this.movementHunterMonkey(detour);
			}catch(CollisionException stillBlocked){
				// Boxed in: wait for the next tick.
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized(Island.LOCK){
			try{
				this.chase();
			}catch(NullPointerException n){
				this.getTimer().stop();
			}
		}
	}

}
