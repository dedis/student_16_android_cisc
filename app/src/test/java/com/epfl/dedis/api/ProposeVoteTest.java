package com.epfl.dedis.api;

import com.epfl.dedis.net.Identity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(JUnit4.class)
public class ProposeVoteTest extends APITest{

    @Test
    public void successfulVotingOneDeviceSkipchain() {
        Identity identity = new CreateIdentity(activity, NAME1, cothority(HOST, PORT), true).getIdentity();
        identity.updateData("this is data");
        new ProposeSend(activity, identity, true);
        identity.setProposed(null);
        new ProposeUpdate(activity, identity, true);
        new ProposeVote(activity, identity, true);
        new ConfigUpdate(activity, identity, true);

        assertEquals("this is data", identity.getConfig().getData().get(NAME1));
    }

    @Test
    public void successfulVotingTwoDeviceSkipchain() {
        Identity identity1 = new CreateIdentity(activity, NAME1, cothority(HOST, PORT), true).getIdentity();

        Identity identity2 = new Identity(cothority(HOST, PORT), identity1.getId());
        new ConfigUpdate(activity, identity2, true);
        assertNotNull(identity2.getConfig());

        identity2.newDevice(NAME2);
        assertNotNull(identity2.getProposed());
        new ProposeSend(activity, identity2, true);
        new ProposeVote(activity, identity2, true);
        new ConfigUpdate(activity, identity2, true);
        assertFalse(identity2.getConfig().getDevice().containsKey(NAME2));


        new ProposeUpdate(activity, identity1, true);
        assertNotNull(identity1.getProposed());
        assertTrue(identity1.getProposed().getDevice().containsKey(NAME2));
        new ProposeVote(activity, identity1, true);

        new ConfigUpdate(activity, identity2, true);
        assertTrue(identity2.getConfig().getDevice().containsKey(NAME2));
    }

    @Test
    public void successfulVotingThreeDeviceSkipchain() {
        Identity identity1 = new CreateIdentity(activity, NAME1, cothority(HOST, PORT), true).getIdentity();
        Identity identity2 = new Identity(cothority(HOST, PORT), identity1.getId());
        Identity identity3 = new Identity(cothority(HOST, PORT), identity2.getId());

        new ConfigUpdate(activity, identity2, true);
        identity2.newDevice(NAME2);
        new ProposeSend(activity, identity2, true);
        new ProposeVote(activity, identity2, true);

        new ProposeUpdate(activity, identity1, true);
        new ProposeVote(activity, identity1, true);

        new ConfigUpdate(activity, identity3, true);
        assertEquals(2, identity3.getConfig().getDevice().size());

        identity3.newDevice(NAME3);
        new ProposeSend(activity, identity3, true);
        new ProposeVote(activity, identity3, true);
        new ConfigUpdate(activity, identity3, true);
        assertFalse(identity3.getConfig().getDevice().containsKey(NAME3));

        new ProposeUpdate(activity, identity1, true);
        assertTrue(identity1.getProposed().getDevice().containsKey(NAME3));
        new ProposeUpdate(activity, identity2, true);
        assertTrue(identity2.getProposed().getDevice().containsKey(NAME3));

        new ProposeVote(activity, identity1, true);
        new ProposeVote(activity, identity2, true);

        new ConfigUpdate(activity, identity3, true);
        assertTrue(identity3.getConfig().getDevice().containsKey(NAME3));
    }
}