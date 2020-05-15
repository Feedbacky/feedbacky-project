import React, {useContext} from 'react';
import {Col} from "react-bootstrap";
import {increaseBrightness, isHexDark} from "components/util/utils";
import AppContext from "context/app-context";
import {FaAt, FaColumns, FaRegEnvelope, FaSlidersH, FaTags, FaUsersCog} from "react-icons/all";
import {renderSidebarRoutes} from "components/sidebar/sidebar-commons";

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
        </ul>
    </Col>
};

export default AdminSidebar;