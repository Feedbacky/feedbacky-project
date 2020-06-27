import React, {useContext} from 'react';
import {Col} from "react-bootstrap";
import ProfileSidebar from "components/sidebar/profile-sidebar";
import ExploreElement from "components/profile/explore-element";
import ViewBox from "components/viewbox/view-box";
import AppContext from "context/app-context";

const ExploreView = (props) => {
    const context = useContext(AppContext);
    return <React.Fragment>
        <ProfileSidebar currentNode="explore" reRouteTo={props.reRouteTo}/>
        <Col xs={12} md={9}>
            <ViewBox theme={context.getTheme(false)} title="Explore More Boards" description="Discover other Feedbacky boards here.">
                <ExploreElement/>
            </ViewBox>
        </Col>
    </React.Fragment>
};

export default ExploreView;