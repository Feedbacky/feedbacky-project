import React, {useContext} from 'react';
import {Col} from "react-bootstrap";
import {increaseBrightness, isHexDark} from "components/util/utils";
import AppContext from "context/app-context";
import {FaAt, FaColumns, FaDiscord, FaQuestionCircle, FaRegCommentDots, FaRegEnvelope, FaSlidersH, FaTags, FaUsersCog} from "react-icons/all";
import {renderSidebarRoutes} from "components/sidebar/sidebar-commons";
import Attribution from "components/util/attribution";

const AdminSidebar = (props) => {
    const routes = [
        {general: (data) => <React.Fragment><FaSlidersH className="mr-1 move-top-1px" style={data}/> General</React.Fragment>},
        {tags: (data) => <React.Fragment><FaTags className="mr-1 move-top-1px" style={data}/> Tags</React.Fragment>},
        {social: (data) => <React.Fragment><FaAt className="mr-1 move-top-1px" style={data}/> Social Links</React.Fragment>},
        {webhooks: (data) => <React.Fragment><FaColumns className="mr-1 move-top-1px" style={data}/> Webhooks</React.Fragment>},
        {moderators: (data) => <React.Fragment><FaUsersCog className="mr-1 move-top-1px" style={data}/> Moderators</React.Fragment>},
        {invitations: (data) => <React.Fragment><FaRegEnvelope className="mr-1 move-top-1px" style={data}/> Invitations</React.Fragment>}
    ];
    const context = useContext(AppContext);
    const themeColor = isHexDark(context.theme) && context.user.darkMode ? increaseBrightness(context.theme, 40) : context.theme;

    return <Col xs={12} md={3} className="mt-4" id="sidebar">
        <ul className="pl-0 mb-1" style={{listStyle: "none", fontSize: "1.1rem", fontWeight: 500, lineHeight: "2rem"}}>
            {renderSidebarRoutes(routes, themeColor, props.currentNode, props.reRouteTo)}
            <li className="my-4"/>
            <li>
                <a href="https://app.feedbacky.net/b/feedbacky-official" className="text-black-75">
                    <FaRegCommentDots className="mr-1 text-black-50 move-top-1px"/> Feedback
                </a>
            </li>
            <li>
                <a href="https://docs.feedbacky.net" className="text-black-75">
                    <FaQuestionCircle className="mr-1 text-black-50 move-top-1px"/> FAQ
                </a>
            </li>
            <li>
                <a href="https://discordapp.com/invite/6qCnKh5" className="text-black-75">
                    <FaDiscord className="mr-1 text-black-50 move-top-1px"/> Discord Support
                </a>
            </li>
        </ul>
        <Attribution/>
    </Col>
};

export default AdminSidebar;