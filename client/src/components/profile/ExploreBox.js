import styled from "@emotion/styled";
import AppContext from "context/AppContext";
import React, {useContext} from 'react';
import {Link} from "react-router-dom";
import {UiCard} from "ui";

const ExploreCard = styled(UiCard)`
  margin: .5rem;
  background-image: url("${props => props.banner}");
  background-size: cover;
  min-width: 220px !important;
  width: 220px !important;
  color: white;
  text-shadow: 0 0 4px black;
  text-align: center;
  
`;

const ExploreBox = (props) => {
    const {discriminator, banner, logo, name, description} = props;
    const {defaultTheme} = useContext(AppContext);
    const border = "2px solid " + defaultTheme;
    return <Link to={"/b/" + discriminator}>
        <ExploreCard banner={banner} style={{border}}>
            <img alt='Board' src={logo} width='60px' height='60px' className="mb-1"/>
            <br/>
            <span className='h4-responsive text-truncate' style={{fontWeight: 500}}>{name}</span>
            <p className='px-2 mb-0 small text-truncate' style={{letterSpacing: "-.35pt"}} dangerouslySetInnerHTML={{__html: description}}/>
        </ExploreCard>
    </Link>
};

export default ExploreBox;