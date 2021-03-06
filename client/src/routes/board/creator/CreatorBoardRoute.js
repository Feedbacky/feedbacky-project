import axios from "axios";
import ProfileNavbar from "components/profile/ProfileNavbar";
import {AppContext} from "context";
import {Step} from "rc-steps";
import React, {useContext, useEffect, useState} from 'react';
import {FaSignInAlt} from "react-icons/all";
import {Link, useHistory} from "react-router-dom";
import StepFirstSubroute from "routes/board/creator/StepFirstSubroute";
import StepSecondSubroute from "routes/board/creator/StepSecondSubroute";
import StepThirdSubroute from "routes/board/creator/StepThirdSubroute";
import ErrorRoute from "routes/ErrorRoute";
import tinycolor from "tinycolor2";
import {UiProgressBar} from "ui";
import {UiCancelButton, UiLoadableButton, UiNextStepButton, UiPreviousStepButton} from "ui/button";
import {UiCol, UiContainer, UiRow} from "ui/grid";
import {hideNotifications, popupNotification, popupWarning} from "utils/basic-utils";
import {useTitle} from "utils/use-title";

const CreatorBoardRoute = () => {
    const context = useContext(AppContext);
    const {getTheme} = context;
    const {onThemeChange, user} = context;
    const history = useHistory();
    const [settings, setSettings] = useState({step: 1, name: "", discriminator: "", banner: null, logo: null, themeColor: "#2d3436"});
    useEffect(() => onThemeChange(), [onThemeChange]);
    useTitle("Create New Board");
    if (!user.loggedIn) {
        return <ErrorRoute Icon={FaSignInAlt} message="You Must Be Logged To Do That"/>
    }
    const updateSettings = (data) => {
        setSettings(data);
    };
    const renderStep = () => {
        switch (settings.step) {
            case 1:
                return <StepFirstSubroute updateSettings={updateSettings} settings={settings}/>;
            case 2:
                return <StepSecondSubroute updateSettings={updateSettings} settings={settings}/>;
            case 3:
                return <StepThirdSubroute updateSettings={updateSettings} settings={settings}/>;
            default:
                popupWarning("Encountered unexpected issue");
                return <StepFirstSubroute updateSettings={updateSettings} settings={settings}/>;
        }
    };
    const renderBackButton = () => {
        if (settings.step === 1) {
            return <React.Fragment/>
        }
        return <UiPreviousStepButton previousStep={previousStep}/>
    };
    const renderNextButton = () => {
        if (settings.step >= 3) {
            const onFinish = () => axios.post("/boards/", {
                discriminator: settings.discriminator,
                name: settings.name,
                shortDescription: settings.name + " feedback",
                fullDescription: "Feedback for **" + settings.name + "** project." +
                    " " +
                    "Edit this description in admin panel.",
                themeColor: settings.themeColor,
                banner: settings.banner,
                logo: settings.logo,
            }).then(res => {
                if (res.status !== 201) {
                    popupWarning("Couldn't create new board due to unknown error!");
                    return;
                }
                popupNotification("Board created", getTheme());
                history.push("/b/" + settings.discriminator);
            });
            return <UiLoadableButton label={"Create Board"} color={tinycolor("#00c851")} className={"ml-2"} onClick={onFinish}>Create Board</UiLoadableButton>
        }
        return <UiNextStepButton nextStep={nextStep}/>
    };
    const previousStep = () => {
        setSettings({...settings, step: settings.step - 1});
    };
    const nextStep = () => {
        if (settings.step === 1) {
            if (settings.name === "" || settings.discriminator === "") {
                popupWarning("Values must not be empty");
                return;
            }
            if (settings.name.length < 4) {
                popupWarning("Board name must be longer");
                return;
            }
            if (settings.discriminator.length < 3) {
                popupWarning("Discriminator must be longer");
                return;
            }
            axios.get("/boards/" + settings.discriminator).then(res => {
                if (res.status === 200) {
                    popupWarning("This discriminator is taken");
                }
            }).catch(() => {
                setSettings({...settings, step: settings.step + 1});
                hideNotifications();
            });
            return;
        }
        setSettings({...settings, step: settings.step + 1});
    };
    const calculateOwnedBoards = () => {
        let owned = 0;
        user.data.permissions.forEach(board => {
            if (board.role.toLowerCase() === "owner") {
                owned++;
            }
        });
        return owned;
    };
    if (calculateOwnedBoards() >= 5) {
        return <React.Fragment>
            <ProfileNavbar/>
            <UiContainer>
                <UiCol xs={12} className="mt-5 pt-5 text-center">
                    <img alt="" src="https://cdn.feedbacky.net/static/svg/undraw_project_limit.svg" className="my-2" width={150} height={150}/>
                    <h2 className="text-red">Boards Limit Reached</h2>
                    <span className="text-black-60">
                        Cannot create any more boards.
                    </span>
                </UiCol>
            </UiContainer>
        </React.Fragment>
    }
    return <React.Fragment>
        <ProfileNavbar/>
        <UiContainer>
            <UiRow className={"mt-4 mt-sm-5 mb-5"}>
                <UiProgressBar currentStep={settings.step} steps={4}>
                    <Step title={"Choose Name"}/>
                    <Step title={"Brand Board"}/>
                    <Step title={"Select Theme"}/>
                    <Step title={"Finish"} state={"finish"}/>
                </UiProgressBar>
                {renderStep()}
                <UiCol xs={12} className={"text-right mt-4"}>
                    <UiCancelButton as={Link} to={"/me"}>Cancel</UiCancelButton>
                    {renderBackButton()}
                    {renderNextButton()}
                </UiCol>
            </UiRow>
        </UiContainer>
    </React.Fragment>
};

export default CreatorBoardRoute;