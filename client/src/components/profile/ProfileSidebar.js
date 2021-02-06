import Attribution from "components/commons/Attribution";
import {renderSidebarRoutes, Sidebar, SidebarIcon} from "components/commons/sidebar-commons";
import AppContext from "context/AppContext";
import React, {useContext} from 'react';
import {FaRegAddressCard, FaRegBell, FaRegKeyboard, FaSearch} from "react-icons/all";
import {UiCol} from "ui/grid";

export const PROFILE_LIGHT_THEME_COLOR = "#008033";
export const PROFILE_DARK_THEME_COLOR = "#00e25b";

const ProfileSidebar = ({currentNode, reRouteTo}) => {
    const routes = [
        {settings: data => <React.Fragment><SidebarIcon as={FaRegAddressCard} style={data}/> Settings</React.Fragment>},
        {explore: data => <React.Fragment><SidebarIcon as={FaSearch} style={data}/> Explore</React.Fragment>},
        {appearance: data => <React.Fragment><SidebarIcon as={FaRegKeyboard} style={data}/> Appearance</React.Fragment>},
        {notifications: data => <React.Fragment><SidebarIcon as={FaRegBell} style={data}/> Notifications</React.Fragment>},
    ];
    const {user} = useContext(AppContext);
    const themeColor = user.darkMode ? PROFILE_DARK_THEME_COLOR : PROFILE_LIGHT_THEME_COLOR;
    return <UiCol xs={12} md={3} as={Sidebar}>
        <ul>{renderSidebarRoutes(routes, themeColor, currentNode, reRouteTo)}</ul>
        <Attribution/>
    </UiCol>
};

export default ProfileSidebar;