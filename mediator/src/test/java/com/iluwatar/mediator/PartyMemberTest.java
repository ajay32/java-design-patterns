package com.iluwatar.mediator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Date: 12/19/15 - 10:13 PM
 *
 * @author Jeroen Meulemeester
 */
@RunWith(Parameterized.class)
public class PartyMemberTest {

  @Parameterized.Parameters
  public static Collection<Supplier<PartyMember>[]> data() {
    return Arrays.asList(
            new Supplier[]{Hobbit::new},
            new Supplier[]{Hunter::new},
            new Supplier[]{Rogue::new},
            new Supplier[]{Wizard::new}
    );
  }

  /**
   * The mocked standard out {@link PrintStream}, required since some actions on a {@link
   * PartyMember} have any influence on any other accessible objects, except for writing to std-out
   * using {@link System#out}
   */
  private final PrintStream stdOutMock = mock(PrintStream.class);

  /**
   * Keep the original std-out so it can be restored after the test
   */
  private final PrintStream stdOutOrig = System.out;

  /**
   * Inject the mocked std-out {@link PrintStream} into the {@link System} class before each test
   */
  @Before
  public void setUp() {
    System.setOut(this.stdOutMock);
  }

  /**
   * Removed the mocked std-out {@link PrintStream} again from the {@link System} class
   */
  @After
  public void tearDown() {
    System.setOut(this.stdOutOrig);
  }

  /**
   * The factory, used to create a new instance of the tested party member
   */
  private final Supplier<PartyMember> memberSupplier;

  /**
   * Create a new test instance, using the given {@link PartyMember} factory
   *
   * @param memberSupplier The party member factory
   */
  public PartyMemberTest(final Supplier<PartyMember> memberSupplier) {
    this.memberSupplier = memberSupplier;
  }

  /**
   * Verify if a party action triggers the correct output to the std-Out
   */
  @Test
  public void testPartyAction() {
    final PartyMember member = this.memberSupplier.get();

    for (final Action action : Action.values()) {
      member.partyAction(action);
      verify(this.stdOutMock).println(member.toString() + " " + action.getDescription());
    }

    verifyNoMoreInteractions(this.stdOutMock);
  }

  /**
   * Verify if a member action triggers the expected interactions with the party class
   */
  @Test
  public void testAct() {
    final PartyMember member = this.memberSupplier.get();

    member.act(Action.GOLD);
    verifyZeroInteractions(this.stdOutMock);

    final Party party = mock(Party.class);
    member.joinedParty(party);
    verify(this.stdOutMock).println(member.toString() + " joins the party");

    for (final Action action : Action.values()) {
      member.act(action);
      verify(this.stdOutMock).println(member.toString() + " " + action.toString());
      verify(party).act(member, action);
    }

    verifyNoMoreInteractions(party, this.stdOutMock);
  }

  /**
   * Verify if {@link PartyMember#toString()} generate the expected output
   */
  @Test
  public void testToString() throws Exception {
    final PartyMember member = this.memberSupplier.get();
    final Class<? extends PartyMember> memberClass = member.getClass();
    assertEquals(memberClass.getSimpleName(), member.toString());
  }

}
