import axios from "axios";
import ExploreBox from "components/profile/ExploreBox";
import React, {useEffect, useState} from 'react';
import {CardDeck} from "react-bootstrap";
import {UiLoadingSpinner} from "ui";
import {UiContainer, UiRow} from "ui/grid";

const ExploreElement = () => {
    const [featuredBoards, setFeaturedBoards] = useState([]);
    const [loaded, setLoaded] = useState(false);

    useEffect(() => {
        //the only exception for using session storage, not critical part of code
        const featured = sessionStorage.getItem("featuredBoards");
        if (featured !== null) {
            setFeaturedBoards(JSON.parse(featured));
            setLoaded(true);
            return;
        }
        axios.get("/featuredBoards")
            .then(res => {
                const data = res.data;
                sessionStorage.setItem("featuredBoards", JSON.stringify(data));
                setFeaturedBoards(data);
                setLoaded(true);
            });
    }, []);

    const renderFeaturedBoards = () => {
        if (!loaded) {
            return <div className="my-5"><UiLoadingSpinner/></div>
        }
        return featuredBoards.map(boardData => {
            return <ExploreBox key={boardData.id} name={boardData.name} description={boardData.shortDescription}
                               banner={boardData.banner} logo={boardData.logo}
                               discriminator={boardData.discriminator}/>
        });
    };

    return <UiContainer>
        <UiRow centered className="text-center">
            <CardDeck id="profile-featured" className="col-12 row justify-content-center">
                {renderFeaturedBoards()}
            </CardDeck>
        </UiRow>
    </UiContainer>
};

export default ExploreElement;