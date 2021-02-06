import {renderLogIn} from "components/commons/navbar-commons";
import AppContext from "context/AppContext";
import React, {useContext} from 'react';
import {UiContainer} from "ui/grid";
import {UiAvatar} from "ui/image";
import {UiNavbar, UiNavbarBrand} from "ui/navbar";

const ProfileNavbar = ({onNotLoggedClick}) => {
    const context = useContext(AppContext);
    const {user, getTheme} = context;
    const theme = getTheme(false);
    const renderHello = () => {
        if (!user.loggedIn) {
            return <React.Fragment>
                <UiAvatar rounded className={"mr-2"} size={30} user={null}/>
                Hello Anonymous
            </React.Fragment>
        }
        return <React.Fragment>
            <UiAvatar className={"mr-2"} roundedCircle user={user.data} size={30}/>
            Hello {user.data.username}
        </React.Fragment>
    };

    return <UiNavbar theme={theme}>
        <UiContainer className={"d-flex"}>
            <UiNavbarBrand to={"/me"}>
                {renderHello(context)}
            </UiNavbarBrand>
            {renderLogIn(onNotLoggedClick, context)}
        </UiContainer>
    </UiNavbar>
};

export default ProfileNavbar;