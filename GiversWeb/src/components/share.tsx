import { Grid, GridList } from '@material-ui/core';
import React, { Component } from 'react';
import {
  FacebookShareButton,
  FacebookMessengerShareButton,
  FacebookMessengerIcon,
  LinkedinShareButton,
  TwitterShareButton,
  PinterestShareButton,
  VKShareButton,
  OKShareButton,
  TelegramShareButton,
  WhatsappShareButton,
  RedditShareButton,
  EmailShareButton,
  TumblrShareButton,
  LivejournalShareButton,
  MailruShareButton,
  ViberShareButton,
  WorkplaceShareButton,
  LineShareButton,
  WeiboShareButton,
  PocketShareButton,
  InstapaperShareButton,
  HatenaShareButton,
  FacebookIcon,
  TwitterIcon,
  LinkedinIcon,
  PinterestIcon,
  VKIcon,
  OKIcon,
  TelegramIcon,
  WhatsappIcon,
  RedditIcon,
  TumblrIcon,
  MailruIcon,
  EmailIcon,
  LivejournalIcon,
  ViberIcon,
  WorkplaceIcon,
  LineIcon,
  PocketIcon,
  InstapaperIcon,
  WeiboIcon,
  HatenaIcon,
} from 'react-share'; //https://www.npmjs.com/package/react-share

function Share(props) {
    const shareUrl = props.url;
    const title = 'Vê esta ação de Voluntariado na app Givers!';
    const spacing = 2;
    const size = 50;
    return (
        
      <div className="Demo__container">
        <Grid container>
        <Grid item xs={spacing}>
          <FacebookShareButton
            url={shareUrl}
            quote={title}
            className="Demo__some-network__share-button"
          >
            <FacebookIcon size={size} round />
          </FacebookShareButton>

        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <FacebookMessengerShareButton
            url={shareUrl}
            appId="521270401588372"
            className="Demo__some-network__share-button"
          >
            <FacebookMessengerIcon size={size} round />
          </FacebookMessengerShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <TwitterShareButton
            url={shareUrl}
            title={title}
            className="Demo__some-network__share-button">
          
            <TwitterIcon size={size} round />
          </TwitterShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <TelegramShareButton
            url={shareUrl}
            title={title}
            className="Demo__some-network__share-button"
          >
            <TelegramIcon size={size} round />
          </TelegramShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <WhatsappShareButton
            url={shareUrl}
            title={title}
            separator=":: "
            className="Demo__some-network__share-button"
          >
            <WhatsappIcon size={size} round />
          </WhatsappShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <LinkedinShareButton url={shareUrl} className="Demo__some-network__share-button">
            <LinkedinIcon size={size} round />
          </LinkedinShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <PinterestShareButton
            url={String(window.location)}
            media={`https://www.google.com/url?sa=i&url=https%3A%2F%2Fpixabay.com%2Fimages%2Fsearch%2Fnature%2F&psig=AOvVaw173pBTYeFql079H-5wBR_5&ust=16212683size490000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCOiJwKjNzvACFQAAAAAdAAAAABAD`}
            className="Demo__some-network__share-button"
          >
            <PinterestIcon size={size} round />
          </PinterestShareButton>

        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <VKShareButton
            url={shareUrl}
            image={`https://www.google.com/url?sa=i&url=https%3A%2F%2Fpixabay.com%2Fimages%2Fsearch%2Fnature%2F&psig=AOvVaw173pBTYeFql079H-5wBR_5&ust=16212683size490000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCOiJwKjNzvACFQAAAAAdAAAAABAD`}
            className="Demo__some-network__share-button"
          >
            <VKIcon size={size} round />
          </VKShareButton>

        </Grid>


        <Grid item xs={spacing} className="Demo__some-network">
          <RedditShareButton
            url={shareUrl}
            title={title}
            windowWidth={660}
            windowHeight={460}
            className="Demo__some-network__share-button"
          >
            <RedditIcon size={size} round />
          </RedditShareButton>

        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <TumblrShareButton
            url={shareUrl}
            title={title}
            className="Demo__some-network__share-button"
          >
            <TumblrIcon size={size} round />
          </TumblrShareButton>

        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <LivejournalShareButton
            url={shareUrl}
            title={title}
            description={shareUrl}
            className="Demo__some-network__share-button"
          >
            <LivejournalIcon size={size} round />
          </LivejournalShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <EmailShareButton
            url={shareUrl}
            subject={title}
            body="body"
            className="Demo__some-network__share-button"
          >
            <EmailIcon size={size} round />
          </EmailShareButton>
        </Grid>
        <Grid item xs={spacing} className="Demo__some-network">
          <ViberShareButton
            url={shareUrl}
            title={title}
            className="Demo__some-network__share-button"
          >
            <ViberIcon size={size} round />
          </ViberShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <WorkplaceShareButton
            url={shareUrl}
            quote={title}
            className="Demo__some-network__share-button"
          >
            <WorkplaceIcon size={size} round />
          </WorkplaceShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <LineShareButton
            url={shareUrl}
            title={title}
            className="Demo__some-network__share-button"
          >
            <LineIcon size={size} round />
          </LineShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <WeiboShareButton
            url={shareUrl}
            title={title}
            image={`https://www.google.com/url?sa=i&url=https%3A%2F%2Fpixabay.com%2Fimages%2Fsearch%2Fnature%2F&psig=AOvVaw173pBTYeFql079H-5wBR_5&ust=16212683size490000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCOiJwKjNzvACFQAAAAAdAAAAABAD`}
            className="Demo__some-network__share-button"
          >
            <WeiboIcon size={size} round />
          </WeiboShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <PocketShareButton
            url={shareUrl}
            title={title}
            className="Demo__some-network__share-button"
          >
            <PocketIcon size={size} round />
          </PocketShareButton>
        </Grid>

        <Grid item xs={spacing} className="Demo__some-network">
          <InstapaperShareButton
            url={shareUrl}
            title={title}
            className="Demo__some-network__share-button"
          >
            <InstapaperIcon size={size} round />
          </InstapaperShareButton>
        </Grid>

        </Grid>
      </div>
    );
  
}

export default Share;