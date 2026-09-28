package fr.eseo.command.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import fr.eseo.command.Manager;
import fr.eseo.communication.Client;
import fr.eseo.communication.ServiceMonkeyIsland;
import fr.eseo.model.CrazyMonkey;
import fr.eseo.model.Island;
import fr.eseo.model.Pirate;
import fr.eseo.model.StatePirate;

/**
 * Tests of the Manager, the per-client game controller.
 */
public class TestManager {

	private static final int PORT = 4242;

	private MockedStatic<Island> islandStatic;

	private Island island;

	private CrazyMonkey monkey;

	private Client client;

	private ServiceMonkeyIsland service;

	private List<String> sent;

	private Manager manager;

	@Before
	public void setUp(){
		this.island = new Island();
		this.island.setIsland(10, 10);
		this.monkey = new CrazyMonkey(1, 1);
		this.monkey.getTimer().stop();
		this.island.getMonkeys().add(this.monkey);
		this.islandStatic = Mockito.mockStatic(Island.class);
		this.islandStatic.when(Island::getInstance).thenReturn(this.island);

		this.sent = new ArrayList<String>();
		this.service = mock(ServiceMonkeyIsland.class);
		this.client = mock(Client.class);
		when(this.client.donneId()).thenReturn("Socket[addr=/127.0.0.1,port=" + PORT + ",localport=13579]");
		when(this.client.getMonkeyIsland()).thenReturn(this.service);
		doAnswer(invocation -> this.sent.add(invocation.getArgument(0))).when(this.client).envoieMessage(anyString());

		this.manager = new Manager(this.client);
		this.manager.inscription();
	}

	@After
	public void tearDown(){
		this.manager.quitte();
		this.islandStatic.close();
	}

	private Pirate pirate(){
		return this.island.getPirates().get(0);
	}

	@Test
	public void testInscriptionCreatesOnePirateAndAnnouncesIt(){
		assertEquals(1, this.island.getPirates().size());
		assertEquals(PORT, this.pirate().getId());
		verify(this.service).inscriptionCanal(this.client);
		verify(this.service).diffuseAutres(Mockito.startsWith("/n " + PORT + "-"), Mockito.eq(this.client));
		assertTrue(this.sent.stream().anyMatch(m -> m.startsWith("/i " + PORT + "-")));
		assertTrue(this.sent.contains("/P "));
	}

	@Test
	public void testMessagesDoNotLeakObservers(){
		for(int i = 0; i < 5; i++){
			this.manager.setMessage(i % 2 == 0 ? "/D 1 0" : "/D -1 0");
			this.manager.movePirate();
		}
		this.manager.inscription();
		assertEquals(1, this.monkey.countObservers());
		assertEquals(1, this.island.getPirates().size());
	}

	@Test
	public void testMoveIsAcceptedOnceAndBroadcast(){
		final Pirate pirate = this.pirate();
		final int direction = pirate.getCoordinateX() < 5 ? 1 : -1;
		this.sent.clear();
		this.manager.setMessage(direction == 1 ? "/D 1 0" : "/D -1 0");
		this.manager.movePirate();
		assertEquals(1, this.sent.size());
		assertTrue(this.sent.get(0).startsWith("/A "));
		verify(this.service).diffuseAutres("/p " + PORT + "-" + pirate.getCoordinateX() + "-" + pirate.getCoordinateY(), this.client);
	}

	@Test
	public void testDeadPirateMoveIsRefused(){
		this.pirate().setState(StatePirate.dead);
		this.sent.clear();
		this.manager.setMessage("/D 1 0");
		this.manager.movePirate();
		assertEquals("/R ", this.sent.get(this.sent.size() - 1));
	}

	@Test
	public void testQuitRemovesThePirate(){
		this.manager.quitte();
		assertEquals(0, this.island.getPirates().size());
		assertEquals(0, this.monkey.countObservers());
		verify(this.service).diffuseAutres("/s " + PORT, this.client);
	}
}
