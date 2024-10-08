/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.builder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

/**
 * Builder to construct Group objects
 *
 * @author Tom Desair (tom dot desair at atmire dot com)
 * @author Raf Ponsaerts (raf dot ponsaerts at atmire dot com)
 */
public class GroupBuilder extends AbstractDSpaceObjectBuilder<Group> {

    private Group group;

    protected GroupBuilder(Context context) {
        super(context);

    }

    @Override
    public void cleanup() throws Exception {
        try (Context c = new Context()) {
            c.setDispatcher("noindex");
            c.turnOffAuthorisationSystem();
            // Ensure object and any related objects are reloaded before checking to see what needs cleanup
            group = c.reloadEntity(group);
            if (group != null) {
                delete(c, group);
                c.complete();
            }
        }
    }

    public static GroupBuilder createGroup(final Context context) {
        GroupBuilder builder = new GroupBuilder(context);
        return builder.create(context);
    }

    public static GroupBuilder createCollectionAdminGroup(final Context context, Collection collection) {
        GroupBuilder builder = new GroupBuilder(context);
        return builder.createAdminGroup(context, collection);
    }

    public static GroupBuilder createCollectionSubmitterGroup(final Context context, Collection collection) {
        GroupBuilder builder = new GroupBuilder(context);
        return builder.createSubmitterGroup(context, collection);
    }

    public static GroupBuilder createCollectionDefaultReadGroup(final Context context, Collection collection,
                                                                String typeOfGroupString, int defaultRead) {
        GroupBuilder builder = new GroupBuilder(context);
        return builder.createDefaultReadGroup(context, collection, typeOfGroupString, defaultRead);
    }

    public static GroupBuilder createCollectionWorkflowRoleGroup(final Context context, Collection collection,
                                                                String roleName) {
        GroupBuilder builder = new GroupBuilder(context);
        return builder.createWorkflowRoleGroup(context, collection, roleName);
    }

    public static GroupBuilder createCommunityAdminGroup(final Context context, Community community) {
        GroupBuilder builder = new GroupBuilder(context);
        return builder.createAdminGroup(context, community);
    }

    private GroupBuilder create(final Context context) {
        this.context = context;
        try {
            group = groupService.create(context);
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    private GroupBuilder createAdminGroup(final Context context, DSpaceObject container) {
        this.context = context;
        try {
            if (container instanceof Collection) {
                group = collectionService.createAdministrators(context, (Collection) container);
            } else if (container instanceof Community) {
                group = communityService.createAdministrators(context, (Community) container);
            } else {
                handleException(new IllegalArgumentException("DSpaceObject must be collection or community. " +
                        "Type: " + container.getType()));
            }
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    private GroupBuilder createSubmitterGroup(final Context context, Collection collection) {
        this.context = context;
        try {
            group = collectionService.createSubmitters(context, collection);
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    private GroupBuilder createDefaultReadGroup(final Context context, Collection collection,
                                                String typeOfGroupString, int defaultRead) {
        this.context = context;
        try {
            group = collectionService.createDefaultReadGroup(context, collection, typeOfGroupString, defaultRead);
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    private GroupBuilder createWorkflowRoleGroup(final Context context, Collection collection, String roleName) {
        this.context = context;
        try {
            group = workflowService.createWorkflowRoleGroup(context, collection, roleName);
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    @Override
    protected DSpaceObjectService<Group> getService() {
        return groupService;
    }

    @Override
    public Group build() {
        try {
            groupService.update(context, group);
        } catch (Exception e) {
            return handleException(e);
        }
        return group;
    }

    public GroupBuilder withName(String groupName) {
        try {
            groupService.setName(group, groupName);
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    public GroupBuilder withParent(Group parent) {
        try {
            groupService.addMember(context, parent, group);
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    public GroupBuilder addMember(EPerson eperson) {
        try {
            groupService.addMember(context, group, eperson);
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    public static void deleteGroup(UUID uuid) throws SQLException, IOException {
        try (Context c = new Context()) {
            c.turnOffAuthorisationSystem();
            Group group = groupService.find(c, uuid);
            if (group != null) {
                try {
                    groupService.delete(c, group);
                } catch (AuthorizeException e) {
                    // cannot occur, just wrap it to make the compiler happy
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            c.complete();
        }
    }

}
