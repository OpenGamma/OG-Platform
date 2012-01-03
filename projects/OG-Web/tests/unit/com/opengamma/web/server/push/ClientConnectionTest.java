package com.opengamma.web.server.push;

import com.google.common.collect.Lists;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.rest.MasterType;
import org.mockito.ArgumentMatcher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.time.Instant;
import java.util.Collection;

import static com.opengamma.web.server.push.CollectionMatcher.collectionOf;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test the subscription mechanism of {@link ClientConnection}.  Subscriptions are added for REST URLs.  When any
 * event fires for a particular URL the URL is considered dirty and the client should re-request it.  Therefore there
 * is no need to fire any more events for that URL until the client resubscribes.  So when any event fires for
 * a URL all other subscriptions for that URL should be cleared.
 */
@SuppressWarnings("unchecked")
public class ClientConnectionTest {

  private static final String USER_ID = "USER_ID";
  private static final String CLIENT_ID = "CLIENT_ID";
  private static final String TEST_URL1 = "TEST_URL1";
  private static final String TEST_URL2 = "TEST_URL2";
  private static final UniqueId _uid1 = UniqueId.of("Tst", "101");
  private static final UniqueId _uid2 = UniqueId.of("Tst", "102");

  private RestUpdateListener _listener;
  private ClientConnection _connection;

  @BeforeMethod
  public void setUp() throws Exception {
    _listener = mock(RestUpdateListener.class);
    ConnectionTimeoutTask timeoutTask = mock(ConnectionTimeoutTask.class);
    _connection = new ClientConnection(USER_ID, CLIENT_ID, _listener, mock(ViewportManager.class), timeoutTask);
  }

  /**
   * Checks that a listener to changes to an entity gets fired once and once only.
   */
  @Test
  public void subscribeToEntityEvent() {
    ChangeEvent event = new ChangeEvent(ChangeType.UPDATED, _uid1, _uid1, Instant.now());

    // send an event and make sure the _listener doesn't receive it before subscription
    _connection.entityChanged(event);
    verify(_listener, never()).itemsUpdated(anyCollection());

    // subscribe and verify the _listener receives the event
    _connection.subscribe(_uid1, TEST_URL1);
    _connection.entityChanged(event);
    verify(_listener).itemsUpdated(collectionOf(TEST_URL1));

    // send the event again and make sure the subscription has been cancelled
    _connection.entityChanged(event);
    verifyNoMoreInteractions(_listener);
  }

  @Test
  public void subscribeToMaster() {
    // send an event before subscription and check the listener doesn't receive it
    _connection.masterChanged(MasterType.PORTFOLIO);
    verify(_listener, never()).itemsUpdated(anyCollection());

    // subscribe and verify the listener receives the event
    _connection.subscribe(MasterType.PORTFOLIO, TEST_URL1);
    _connection.masterChanged(MasterType.PORTFOLIO);
    verify(_listener).itemsUpdated(collectionOf(TEST_URL1));

    // send the event again and make sure the subscription has been cancelled
    _connection.masterChanged(MasterType.PORTFOLIO);
    verifyNoMoreInteractions(_listener);
  }

  /**
   * Multiple URLs subscribing to the same entity should produce multiple updates when the entity changes.
   */
  @Test
  public void multipleSubscriptionsToEntity() {
    ChangeEvent event = new ChangeEvent(ChangeType.UPDATED, _uid1, _uid1, Instant.now());

    // subscribe and verify the listener receives the event
    _connection.subscribe(_uid1, TEST_URL1);
    _connection.subscribe(_uid1, TEST_URL2);
    _connection.entityChanged(event);
    verify(_listener).itemsUpdated(collectionOf(TEST_URL1, TEST_URL2));

    // send the event again and make sure the subscription has been cancelled
    _connection.entityChanged(event);
    verifyNoMoreInteractions(_listener);
  }

  /**
   * Multiple URLs subscribing to the same master should produce multiple updates when the master fires a change event.
   */
  @Test
  public void multipleSubscriptionsToMaster() {
    // subscribe and verify the listener receives the event
    _connection.subscribe(MasterType.PORTFOLIO, TEST_URL1);
    _connection.subscribe(MasterType.PORTFOLIO, TEST_URL2);
    _connection.masterChanged(MasterType.PORTFOLIO);
    verify(_listener).itemsUpdated(collectionOf(TEST_URL1, TEST_URL2));

    // send the event again and make sure the subscription has been cancelled
    _connection.masterChanged(MasterType.PORTFOLIO);
    verifyNoMoreInteractions(_listener);
  }

  /**
   * A particular URL should only have one event delivered no matter how many subscriptions it has.  After the
   * first event no more should be triggered until a new subscription is set up.
   */
  @Test
  public void multipeEntitySubscriptionsForSameUrl() {
    ChangeEvent event1 = new ChangeEvent(ChangeType.UPDATED, _uid1, _uid1, Instant.now());
    ChangeEvent event2 = new ChangeEvent(ChangeType.UPDATED, _uid2, _uid2, Instant.now());

    // subscribe and verify the listener receives the event
    _connection.subscribe(_uid1, TEST_URL1);
    _connection.subscribe(_uid2, TEST_URL1);
    _connection.entityChanged(event1);
    verify(_listener).itemsUpdated(collectionOf(TEST_URL1));

    // send an event for the other subscription and check nothing is delivered to the listener
    _connection.entityChanged(event2);
    verifyNoMoreInteractions(_listener);
  }

  /**
   * A particular URL should only have one event delivered no matter how many subscriptions it has.  After the
   * first event no more should be triggered until a new subscription is set up.
   */
  @Test
  public void multipeMasterSubscriptionsForSameUrl() {
    // subscribe and verify the listener receives the event
    _connection.subscribe(MasterType.PORTFOLIO, TEST_URL1);
    _connection.subscribe(MasterType.POSITION, TEST_URL1);
    _connection.masterChanged(MasterType.PORTFOLIO);
    verify(_listener).itemsUpdated(collectionOf(TEST_URL1));

    // send an event for the other subscribed master type and make sure the subscription has been cancelled
    _connection.masterChanged(MasterType.POSITION);
    verifyNoMoreInteractions(_listener);
  }

  /**
   * If there is an entity and master subscription for the same URL then only one event should fire even
   * if both the entity and master are changed.
   */
  @Test
  public void masterAndEntitySubscriptionForSameUrlMasterChangesFirst() {
    _connection.subscribe(_uid1, TEST_URL1);
    _connection.subscribe(MasterType.PORTFOLIO, TEST_URL1);
    _connection.masterChanged(MasterType.PORTFOLIO);
    verify(_listener).itemsUpdated(collectionOf(TEST_URL1));
    _connection.entityChanged(new ChangeEvent(ChangeType.UPDATED, _uid1, _uid1, Instant.now()));
    verifyNoMoreInteractions(_listener);
  }

  /**
   * If there is an entity and master subscription for the same URL then only one event should fire even
   * if both the entity and master are changed.
   */
  @Test
  public void masterAndEntitySubscriptionForSameUrlEntityChangesFirst() {
    _connection.subscribe(_uid1, TEST_URL1);
    _connection.subscribe(MasterType.PORTFOLIO, TEST_URL1);
    _connection.entityChanged(new ChangeEvent(ChangeType.UPDATED, _uid1, _uid1, Instant.now()));
    verify(_listener).itemsUpdated(collectionOf(TEST_URL1));
    _connection.masterChanged(MasterType.PORTFOLIO);
    verifyNoMoreInteractions(_listener);
  }

  /**
   * Checks that nothing happens when a subscription is set up for an entity and a different entity changes.
   */
  @Test
  public void subscriptionForDifferentEntity() {
    _connection.subscribe(_uid1, TEST_URL1);
    _connection.entityChanged(new ChangeEvent(ChangeType.UPDATED, _uid2, _uid2, Instant.now()));
    verifyZeroInteractions(_listener);
  }

  /**
   * Checks that nothing happens when a subscription is set up for a master and a different master changes.
   */
  @Test
  public void subscriptionForDifferentMaster() {
    _connection.subscribe(MasterType.PORTFOLIO, TEST_URL1);
    _connection.masterChanged(MasterType.POSITION);
    verifyZeroInteractions(_listener);
  }
}

/**
 * <p>Matcher for checking an argument to a mock is a collection containing all of the specified items and nothing else.
 * The order of the elements isn't important.</p>

 * <em>I'm surprised this isn't already available in Mockito, although maybe it's there and I just can't find it.</em>

 * @param <T> The type of items in the collection
 */
@SuppressWarnings("unchecked")
class CollectionMatcher<T> extends ArgumentMatcher<Collection<T>> {

  private final Collection<T> _expected;

  public CollectionMatcher(T... expectedItems) {
    _expected = Lists.newArrayList(expectedItems);
  }

  @Override
  public boolean matches(Object o) {
    Collection<T> result = Lists.newArrayList((Collection<T>) o);
    for (T expectedItem : _expected) {
      // every member of the expected collection must be present in the result
      if (!result.remove(expectedItem)) {
        return false;
      }
    }
    // if all members of the expected collection were in the result and after removing them the result is empty
    // the collections must have contained exactly the same elements
    return result.isEmpty();
  }

  static <T> Collection<T> collectionOf(T... items) {
    return argThat(new CollectionMatcher<T>(items));
  }
}
