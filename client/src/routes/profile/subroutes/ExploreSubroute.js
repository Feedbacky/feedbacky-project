import ExploreElement from "components/profile/ExploreElement";
import AppContext from "context/AppContext";
import PageNodesContext from "context/PageNodesContext";
import React, {useContext, useEffect} from 'react';
import {UiCol} from "ui/grid";
import {UiViewBox} from "ui/viewbox";

const ExploreSubroute = () => {
    const context = useContext(AppContext);
    const {setCurrentNode} = useContext(PageNodesContext);
    useEffect(() => setCurrentNode("explore"), [setCurrentNode]);
    return <UiCol xs={12} md={9}>
        <UiViewBox theme={context.getTheme(false)} title="Explore More Boards" description="Discover other Feedbacky boards here.">
            <ExploreElement/>
        </UiViewBox>
    </UiCol>
};

export default ExploreSubroute;