import React from 'react';
import {Card, CardDeck, Col, Row} from "react-bootstrap";
import {FaDiscord, FaGlobe} from "react-icons/fa";
import UndrawCreateProject from "assets/svg/undraw/create_project.svg";

const type = ["DISCORD", "CUSTOM_ENDPOINT"];
const typeName = ["Discord", "Custom Endpoint"];
const typeIcon = [<FaDiscord className="fa-lg"/>, <FaGlobe className="fa-lg"/>];

const StepFirst = (props) => {
    const renderCards = () => {
        return type.map((item, i) => {
            let name = typeName[i];
            let classes = "rounded-xl mb-3";
            if (props.type === item) {
                classes += " border-chosen";
            } else {
                classes += " border-invisible";
            }
            return <Card key={i} className={classes} style={{minWidth: 175}} onClick={() => props.onSetupMethodCall("type", item)}>
                <Card.Body className="text-center">
                    {typeIcon[i]}
                    <br className="my-3"/>
                    <strong style={{fontSize: "1.5rem"}}>{name}</strong>
                </Card.Body>
            </Card>
        });
    };

    return <React.Fragment>
        <Col xs={12} className="mt-4 text-center">
            <img alt="" src={UndrawCreateProject} className="my-2" width={150} height={150}/>
            <h2>Select Webhook Type</h2>
            <span className="text-black-60">
                Select in which way you'll utilize this webhook.
            </span>
        </Col>
        <Col xs={12} className="mt-4 px-md-5 px-3">
            <Row className="justify-content-center">
                <CardDeck className="col-7 justify-content-center">
                    {renderCards()}
                </CardDeck>
            </Row>
        </Col>
    </React.Fragment>;
};

export default StepFirst;