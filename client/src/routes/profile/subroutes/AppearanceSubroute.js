import styled from "@emotion/styled";
import AppearanceCard from "components/profile/AppearanceCard";
import AppContext from "context/AppContext";
import PageNodesContext from "context/PageNodesContext";
import React, {useContext, useEffect} from "react";
import {UiBadge} from "ui";
import {UiCol} from "ui/grid";
import {UiViewBox} from "ui/viewbox";

const InlineCard = styled.div`
  margin-bottom: .5rem;
  display: inline-block;
`;

const AppearanceSubroute = () => {
    const {appearance, onAppearanceToggle, getTheme} = useContext(AppContext);
    const {setCurrentNode} = useContext(PageNodesContext);
    useEffect(() => setCurrentNode("appearance"), [setCurrentNode]);
    const renderContent = () => {
        return <UiCol xs={12} className={"my-2 text-center"}>
            <h4 className={"mb-1"}>Application Theme</h4>
            <InlineCard>
                <AppearanceCard imgSrc={"https://cdn.feedbacky.net/static/img/appearance/system.png"} chosen={appearance.systemDefault} onClick={() => onAppearanceToggle("system")}/>
                <UiBadge>System Default</UiBadge>
            </InlineCard>
            <InlineCard>
                <AppearanceCard imgSrc={"https://cdn.feedbacky.net/static/img/appearance/light.png"} chosen={appearance.mode === "light" && !appearance.systemDefault} onClick={() => onAppearanceToggle("light")}/>
                <UiBadge>Light</UiBadge>
            </InlineCard>
            <InlineCard>
                <AppearanceCard imgSrc={"https://cdn.feedbacky.net/static/img/appearance/dark.png"} chosen={appearance.mode === "dark" && !appearance.systemDefault} onClick={() => onAppearanceToggle("dark")}/>
                <UiBadge>Dark</UiBadge>
            </InlineCard>
        </UiCol>
    };
    return <UiCol xs={12} md={9}>
        <UiViewBox theme={getTheme(false)} title={"Appearance"} description={"Configure how Feedbacky will look like."}>
            {renderContent()}
        </UiViewBox>
    </UiCol>
};

export default AppearanceSubroute;