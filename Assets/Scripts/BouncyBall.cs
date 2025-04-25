using TMPro;
using UnityEngine;

public class BouncyBall : MonoBehaviour
{
    //reference to the RigidBody component of the bouncy ball object
    private Rigidbody2D rigidBody2D;
    public float maxVelocity = 15f;
    //line Y axis threshold below which if the ball passes this value, it reset to its position
    public float minimumY = -5.5f;

    int score = 0;
    int lives = 5;
    int brickCount;

    public TextMeshProUGUI scoreText;
    public GameObject[] livesImage;
    public GameObject gameOverPanel;
    public GameObject youWinPanel;

    public AudioSource src;
    public AudioClip bounceUp, bounceWall, ballFall, brickDestroy, bouncePaddle,gameOver, youWin;

    void Start()
    {
        //get the RigidBody2D component attached to the bouncy ball object
        rigidBody2D = GetComponent<Rigidbody2D>();
        //cound every brick
        brickCount = FindFirstObjectByType<LevelGenerator>().transform.childCount;

        //control the velocity of the ball from the start
        rigidBody2D.linearVelocity = Vector2.down * 10f;
    }


    void Update()
    {

        ResetBall();
        ClampBallVelocity();
      
    }

    void ResetBall()
    {
        //check if the ball's Y position is below the minimum threshold
        if (transform.position.y < minimumY)
        {
            //play fall sound
            src.clip = ballFall;
            src.Play();

            if (lives == 0)
            {
                GameOver();
                return;
            }
            else
            {
 

                // decrease the life of the player
                lives--;
                // when a life has been subtracted, decrease the lives image by setting the visibility of individual image object to false
                livesImage[lives].SetActive(false);

                //resets the ball's position to the center of the viewport
                transform.position = Vector3.zero;

                // reset the ball velocity with the same value from the start
                rigidBody2D.linearVelocity = Vector2.down * 10f;
            }
 

        }
    }

    void ClampBallVelocity()
    {
        if (rigidBody2D.linearVelocity.magnitude > maxVelocity)
        {
            //clamp the magnitude of 'current velocity' of the ball object
            Vector3 clampedVelocity = Vector3.ClampMagnitude(rigidBody2D.linearVelocity, maxVelocity);

            //apply the clampedVelocity 
            rigidBody2D.linearVelocity = clampedVelocity;
        }

    }

    void GameOver()
    {
        Debug.Log("Game Over!");
        gameOverPanel.SetActive(true);

        src.clip = gameOver;
        src.Play();

        //since we are unable to use bouncy ball anymore, we destroy the game object
        Time.timeScale = 0;
        Destroy(gameObject);

    }
    private void OnCollisionEnter2D(Collision2D collision)
    {

        if (collision.gameObject.CompareTag("Paddle"))
        {
            src.clip = bouncePaddle;
            src.Play();
        }

        if (collision.gameObject.CompareTag("Walls"))
        {
            src.clip = bounceWall;
            src.Play();
        }

        //checks if the object that the ball has collided with if it has the tag 'Brick'
        if (collision.gameObject.CompareTag("Brick"))
        {   
            Debug.Log("brick hit!");
            //destroys the object that the ball has collided with
            Destroy(collision.gameObject);
            //decrement every brick that has been counted
            brickCount--;
            src.clip = brickDestroy;
            src.Play();

            //give score to player
            score += 10;
            //when the score is updated, update the score text gui
            scoreText.text = score.ToString("00000");


            if (brickCount == 0) {
                Debug.Log("You Win!");
                youWinPanel.SetActive(true);

                src.clip = youWin;
                src.Play();


                //since we are unable to use bouncy ball anymore, we destroy the game object
                Time.timeScale = 0;
              
            }
        }
    }
}
