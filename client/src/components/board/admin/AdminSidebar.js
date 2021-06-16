import Attribution from "components/commons/Attribution";
import {renderSidebarRoutes, Sidebar, SidebarIcon} from "components/commons/sidebar-commons";
import {AppContext} from "context";
import React, {useContext} from 'react';
import {FaAt, FaColumns, FaDiscord, FaQuestionCircle, FaRegCommentDots, FaSlidersH, FaTags, FaUserLock, FaUsersCog} from "react-icons/all";
import {UiCol} from "ui/grid";

const AdminSidebar = ({currentNode, reRouteTo}) => {
    const routes = [
        {general: data => <React.Fragment><SidebarIcon as={FaSlidersH} style={data}/> General</React.Fragment>},
        {tags: data => <React.Fragment><SidebarIcon as={FaTags} style={data}/> Tags</React.Fragment>},
        {social: data => <React.Fragment><SidebarIcon as={FaAt} style={data}/> Social Links</React.Fragment>},
        {webhooks: data => <React.Fragment><SidebarIcon as={FaColumns} style={data}/> Webhooks</React.Fragment>},
        {moderators: data => <React.Fragment><SidebarIcon as={FaUsersCog} style={data}/> Moderators</React.Fragment>},
        {suspended: data => <React.Fragment><SidebarIcon as={FaUserLock} style={data}/> Suspensions</React.Fragment>}
    ];
    const context = useContext(AppContext);
    const themeColor = context.getTheme();

    return <UiCol xs={12} md={3} as={Sidebar}>
        <ul>
            {renderSidebarRoutes(routes, themeColor, currentNode, reRouteTo)}
            <li className="my-4"/>
            <li>
                <a href="https://app.feedbacky.net/b/feedbacky-official" className="text-black-75">
                    <FaRegCommentDots className="mr-1 text-black-75 move-top-1px"/> Feedback
                </a>
            </li>
            <li>
                <a href="https://docs.feedbacky.net" className="text-black-75">
                    <FaQuestionCircle className="mr-1 text-black-75 move-top-1px"/> FAQ
                </a>
            </li>
            <li>
                <a href="https://discordapp.com/invite/6qCnKh5" className="text-black-75">
                    <FaDiscord className="mr-1 text-black-75 move-top-1px"/> Discord Support
                </a>
            </li>
        </ul>
        <Attribution/>
    </UiCol>
};

export default AdminSidebar;