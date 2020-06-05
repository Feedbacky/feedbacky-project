import React from 'react';
import {Col, Form} from "react-bootstrap";
import {getBase64FromFile, validateImageWithWarning} from "components/util/utils";
import ClickableTip from "components/util/clickable-tip";
import UndrawBrandProject from "assets/svg/undraw/brand_project.svg";

const StepSecond = ({updateSettings, settings}) => {
    const onLogoChange = (e) => {
        if (!validateImageWithWarning(e, "logoInput", 250)) {
            return;
        }
        let file = e.target.files[0];
        getBase64FromFile(file).then(data => {
            document.getElementById("boardLogo").setAttribute("src", data);
            updateSettings({...settings, logo: data});
        });
    };
    const onBannerChange = (e) => {
        if (!validateImageWithWarning(e, "bannerInput", 650)) {
            return;
        }
        let file = e.target.files[0];
        getBase64FromFile(file).then(data => {
            document.getElementById("boardBanner").style["background-image"] = "url('" + data + "')";
            updateSettings({...settings, banner: data});
        });
    };
    const getDefaultBannerUrl = () => {
        if (settings.banner == null) {
            return "https://cdn.feedbacky.net/projects/banners/default-banner.jpg";
        }
        return settings.banner;
    };
    const getDefaultLogoUrl = () => {
        if (settings.logo == null) {
            return "https://cdn.feedbacky.net/projects/logos/default-logo.png";
        }
        return settings.logo
    };
    return <React.Fragment>
        <Col xs={12} className="mt-4 text-center">
            <img alt="" src={UndrawBrandProject} className="my-2" width={150} height={150}/>
            <h2>Brand Your Board</h2>
            <span className="text-black-60">
                Upload your board logo and banner. This step can be skipped and set later.
            </span>
        </Col>
        <Col xs={12} sm={6} className="mt-4 px-md-5 px-3">
            <Form.Label className="mr-1 text-black-60">Board Banner</Form.Label>
            <ClickableTip id="banner" title="Set Board Banner" description={<React.Fragment>
                Upload your board banner.
                <br/>
                <strong>
                    Maximum size 500 kb, png and jpg only.
                    <br/>
                    Suggested size: 1120x400
                </strong>
            </React.Fragment>}/>
            <br/>
            {/* simulate real board jumbotron to show properly sized image */}
            <div id="boardBanner" className="jumbotron mb-2" style={{backgroundImage: `url("` + getDefaultBannerUrl() + `")`}}>
                <h3 className="h3-responsive" style={{color: "transparent"}}>Feedbacky Board</h3>
                <h5 className="h5-responsive" style={{color: "transparent"}}>Feedbacky example Board</h5>
            </div>
            <input className="small" accept="image/jpeg, image/png" id="bannerInput" type="file" name="banner" onChange={e => onBannerChange(e)}/>
        </Col>
        <Col xs={12} sm={6} className="mt-4 px-md-5 px-3">
            <Form.Label className="mr-1 text-black-60">Board Logo</Form.Label>
            <ClickableTip id="logo" title="Set Board Logo" description={<React.Fragment>
                Upload your board logo.
                <br/>
                <strong>
                    Maximum size 150 kb, png and jpg only.
                    <br/>
                    Suggested size: 100x100
                </strong>
            </React.Fragment>}/>
            <br/>
            <img alt="logo" src={getDefaultLogoUrl()} id="boardLogo" className="img-fluid mb-2" width="50px"/>
            <br/>
            <input className="small" accept="image/jpeg, image/png" id="logoInput" type="file" name="logo" onChange={e => onLogoChange(e)}/>
        </Col>
    </React.Fragment>
};

export default StepSecond;