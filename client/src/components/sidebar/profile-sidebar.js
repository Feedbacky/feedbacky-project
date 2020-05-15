import React, {useContext} from 'react';
import {Col} from "react-bootstrap";
import AppContext from "context/app-context";
import {FaRegAddressCard, FaRegBell, FaSearch} from "react-icons/all";
import {renderSidebarRoutes} from "components/sidebar/sidebar-commons";
import Attribution from "components/util/attribution";

const ProfileSidebar = (props) => {
    const routes = [
        {settings: (data) => <React.Fragment><FaRegAddressCard className="mr-1 move-top-1px" style={data}/> Settings</React.Fragment>},
        {explore: (data) => <React.Fragment><FaSearch className="mr-1 move-top-1px" style={data}/> Explore</React.Fragment>},
        {notifications: (data) => <React.Fragment><FaRegBell className="mr-1 move-top-1px" style={data}/> Notifications</React.Fragment>},
    ];
    const context = useContext(AppContext);
    const themeColor = context.user.darkMode ? "#00c851" : "#00a040";

    return <Col xs={12} md={3} className="mt-4" id="sidebar">
        <ul className="pl-0 mb-1" style={{listStyle: "none", fontSize: "1.1rem", fontWeight: 500, lineHeight: "2rem"}}>
            {renderSidebarRoutes(routes, themeColor, props.currentNode, props.reRouteTo)}
        </ul>
        <Attribution/>
    </Col>
};

export default ProfileSidebar;